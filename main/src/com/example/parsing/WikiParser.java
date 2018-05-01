package com.example.parsing;

import java.util.List;

public interface WikiParser {
    List<WikiIntactDocument> parseDocuments(String path);
}
