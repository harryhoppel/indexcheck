package com.example.parsing;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class WikiSaxParser implements WikiParser {
    @Override
    public List<WikiDocument> parseDocuments(String path) {
        List<WikiDocument> documents = new ArrayList<>();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            WikiSaxParserHandler handler = new WikiSaxParserHandler(documents);
            saxParser.parse(new BufferedInputStream(new FileInputStream(new File(path))), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documents;
    }
}
