package com.example.index;

import java.util.List;

public interface InvertedIndex {
    List<WordPosition> getWordPosition(String word);

    void addDelta(IndexDelta delta);
}
