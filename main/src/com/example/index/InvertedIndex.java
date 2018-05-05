package com.example.index;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;

public interface InvertedIndex {
    Set<String> getAllWords();

    SortedSet<WordPosition> getWordPositions(String word);

    void addDelta(IndexDelta delta);

    void dumpToDisk(File path) throws IOException;
}
