package com.example.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryInvertedIndex implements InvertedIndex {
    private ConcurrentHashMap<String, List<WordPosition>> index = new ConcurrentHashMap<>();

    @Override
    public List<WordPosition> getWordPosition(String word) {
        return index.get(word);
    }

    @Override
    public void addDelta(IndexDelta delta) {
        Map<String, List<WordPosition>> miniDeltas = delta.getMiniDeltas();
        for (Map.Entry<String, List<WordPosition>> miniDelta : miniDeltas.entrySet()) {
            String deltaWord = miniDelta.getKey();
            List<WordPosition> wordPositions = new ArrayList<>();
            List<WordPosition> absentWordPositions = index.putIfAbsent(deltaWord, wordPositions);
            if (absentWordPositions == null) {
                wordPositions.addAll(miniDelta.getValue());
            } else {
                absentWordPositions.addAll(miniDelta.getValue());
            }
        }
    }
}
