package com.example.index;

import com.example.index.WordPosition;

import java.util.List;
import java.util.Map;

public class IndexDelta {
    private Map<String, List<WordPosition>> miniDeltas;

    public IndexDelta(Map<String, List<WordPosition>> miniDeltas) {
        this.miniDeltas = miniDeltas;
    }

    public Map<String, List<WordPosition>> getMiniDeltas() {
        return miniDeltas;
    }
}
