package com.example.parsing;

import java.io.File;
import java.util.List;

public interface WikiParser {
    List<WikiIntactDocument> parseDocuments(File path);
}
