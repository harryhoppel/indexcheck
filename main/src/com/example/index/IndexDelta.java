package com.example.index;

import com.example.parsing.WikiProcessedDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexDelta {
    private Map<String, List<WordPosition>> miniDeltas;

    public IndexDelta(WikiProcessedDocument doc, org.tartarus.snowball.ext.russianStemmer russianStemmer) {
        Map<String, List<WordPosition>> miniDeltas = new HashMap<>();
        String processedText = doc.getProcessedText();
        int currentPosition = 0;
        for (String word : processedText.split(" ")) {
            russianStemmer.setCurrent(word);
            russianStemmer.stem();
            word = russianStemmer.getCurrent();
            List<WordPosition> wordPositions = miniDeltas.computeIfAbsent(word, w -> new ArrayList<>());
            wordPositions.add(new WordPosition(doc.getId(), currentPosition));
            currentPosition += word.length() + 1;
        }
        this.miniDeltas = miniDeltas;
    }

    Map<String, List<WordPosition>> getMiniDeltas() {
        return miniDeltas;
    }
}
