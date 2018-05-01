package com.example.parsing;

public class WikiIntactDocument {
    private long id;
    private String text;

    WikiIntactDocument(long id, String text) {
        this.id = id;
        this.text = text;
    }

    long getId() {
        return id;
    }

    String getText() {
        return text;
    }
}
