package com.example.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemoryInvertedIndex implements InvertedIndex {
    private ConcurrentHashMap<String, Gut> index = new ConcurrentHashMap<>();

    @Override
    public Set<String> getAllWords() {
        return new HashSet<>(index.keySet());
    }

    @Override
    public SortedSet<WordPosition> getWordPositions(String word) {
        Gut gut = null;
        try {
            gut = index.get(word);
            gut.getLock().readLock().lock();
            return gut.getPositions();
        } finally {
            if (gut != null) {
                gut.getLock().readLock().unlock();
            }
        }
    }

    @Override
    public void addDelta(IndexDelta delta) {
        Map<String, List<WordPosition>> miniDeltas = delta.getMiniDeltas();
        List<String> wordsToAdd = new ArrayList<>(miniDeltas.keySet());
        while (!wordsToAdd.isEmpty()) {
            for (Iterator<String> iterator = wordsToAdd.iterator(); iterator.hasNext(); ) {
                String deltaWord = iterator.next();
                Gut gutForDelta = index.computeIfAbsent(deltaWord, w -> new Gut(new ReentrantReadWriteLock(), new ConcurrentSkipListSet<>()));
                if (gutForDelta.getLock().writeLock().tryLock()) {
                    gutForDelta.getPositions().addAll(miniDeltas.get(deltaWord));
                    gutForDelta.getLock().writeLock().unlock();
                    iterator.remove();
                } else {
                    Thread.yield();
                }
            }
        }
    }

    @Override
    public void dumpToDisk(File path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
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

    private class Gut {
        private final ReentrantReadWriteLock lock;
        private final SortedSet<WordPosition> positions;

        Gut(ReentrantReadWriteLock lock, SortedSet<WordPosition> positions) {
            this.lock = lock;
            this.positions = positions;
        }

        ReentrantReadWriteLock getLock() {
            return lock;
        }

        SortedSet<WordPosition> getPositions() {
            return positions;
        }
    }
}
