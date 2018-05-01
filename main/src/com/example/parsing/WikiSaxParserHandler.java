package com.example.parsing;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

public class WikiSaxParserHandler extends DefaultHandler {
    private List<WikiDocument> documents;

    private boolean insideTextTag = false;

    public WikiSaxParserHandler(List<WikiDocument> documents) {
        this.documents = documents;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equals("text")) {
            insideTextTag = true;
        } else {
            insideTextTag = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (insideTextTag) {
            documents.add(new WikiDocument(new String(ch)));
        }
    }
}
