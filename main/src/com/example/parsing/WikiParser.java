package com.example.parsing;

import java.util.List;

public interface WikiParser {
    List<WikiDocument> parseDocuments(String path);
}