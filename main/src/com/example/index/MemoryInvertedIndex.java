package com.example.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class MemoryInvertedIndex implements InvertedIndex {
    private ConcurrentHashMap<String, SortedSet<WordPosition>> index = new ConcurrentHashMap<>();

    @Override
    public SortedSet<WordPosition> getWordPositions(String word) {
        return index.get(word);
    }

    @Override
    public void addDelta(IndexDelta delta) {
        Map<String, List<WordPosition>> miniDeltas = delta.getMiniDeltas();
        for (Map.Entry<String, List<WordPosition>> miniDelta : miniDeltas.entrySet()) {
            String deltaWord = miniDelta.getKey();
            SortedSet<WordPosition> wordPositions = index.computeIfAbsent(deltaWord, w -> new ConcurrentSkipListSet<>());
            wordPositions.addAll(miniDelta.getValue());
        }
    }

    @Override
    public void dumpToDisk(String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)))) {
            List<String> indexWords = new ArrayList<>(index.keySet());
            indexWords.sort((o1, o2) -> -Integer.compare(getWordPositions(o1).size(), getWordPositions(o2).size()));
            for (int wordNumber = 0; wordNumber < indexWords.size(); wordNumber++) {
                String indexWord = indexWords.get(wordNumber);
                SortedSet<WordPosition> positions = getWordPositions(indexWord);
                writer.write(indexWord);
                writer.write("\t");
                writer.write("" + positions.size());
                writer.write("\t");
                for (WordPosition wordPosition : positions) {
                    writer.write("" + wordPosition.getDocumentId());
                    writer.write("->");
                    writer.write("" + wordPosition.getDocumentPosition());
                    writer.write(",");
                }
                writer.write("\n");
                if (wordNumber % 10000 == 0) {
                    System.out.println(MessageFormat.format("Dumped index entry {0} / {1}",
                            wordNumber, indexWords.size()));
                }
            }
        }
    }
}
