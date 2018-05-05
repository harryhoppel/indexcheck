package com.example.index;

import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;

public interface InvertedIndex {
    Set<String> getAllWords();

    SortedSet<WordPosition> getWordPositions(String word);

    void addDelta(IndexDelta delta);

    void dumpToDisk(String path) throws IOException;
}
