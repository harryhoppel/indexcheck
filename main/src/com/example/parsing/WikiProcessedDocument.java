package com.example.parsing;

public class WikiProcessedDocument {
    private long id;
    private String processedText;

    public WikiProcessedDocument(WikiIntactDocument document) {
        this.id = document.getId();
        StringBuilder docBuilder = new StringBuilder();
        for (Character c : document.getText().toCharArray()) {
            if (Character.isLetter(c)) {
                docBuilder.append(Character.toLowerCase(c));
            } else {
                if (docBuilder.length() > 0 && docBuilder.charAt(docBuilder.length() - 1) != ' ') {
                    docBuilder.append(' ');
                }
            }
        }
        if (docBuilder.length() > 0 && docBuilder.charAt(docBuilder.length() - 1) == ' ') {
            docBuilder.deleteCharAt(docBuilder.length() - 1);
        }
        this.processedText = docBuilder.toString();
    }

    public long getId() {
        return id;
    }

    public String getProcessedText() {
        return processedText;
    }
}
