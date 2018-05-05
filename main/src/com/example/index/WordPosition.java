package com.example.index;

import java.util.Objects;

public class WordPosition implements Comparable {
    private long documentId;
    private int documentPosition;

    WordPosition(long documentId, int documentPosition) {
        this.documentId = documentId;
        this.documentPosition = documentPosition;
    }

    public long getDocumentId() {
        return documentId;
    }

    public int getDocumentPosition() {
        return documentPosition;
    }

    @Override
    public String toString() {
        return "WPos{" +
                "docId=" + documentId +
                ", docPos=" + documentPosition +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        WordPosition other = (WordPosition) o;
        int compare = Long.compare(documentId, other.documentId);
        if (compare != 0) {
            return compare;
        } else {
            return Integer.compare(documentPosition, other.documentPosition);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordPosition that = (WordPosition) o;
        return documentId == that.documentId &&
                documentPosition == that.documentPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, documentPosition);
    }
}
