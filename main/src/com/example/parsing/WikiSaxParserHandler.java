package com.example.parsing;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

public class WikiSaxParserHandler extends DefaultHandler {
    private List<WikiIntactDocument> documents;

    private boolean insideDocumentTextTag = false;

    private boolean insidePageTag = false;
    private boolean insideIdTag = false;

    private StringBuilder textBuilder;

    private long documentId = -1;

    WikiSaxParserHandler(List<WikiIntactDocument> documents) {
        this.documents = documents;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equals("text")) {
            insideDocumentTextTag = true;
            textBuilder = new StringBuilder();
        }

        if (qName.equals("page")) {
            insidePageTag = true;
        }

        if (insidePageTag && qName.equals("id")) {
            insideIdTag = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("text")) {
            insideDocumentTextTag = false;
            documents.add(new WikiIntactDocument(documentId, textBuilder.toString()));
            documentId = -1;
        }

        if (insidePageTag && qName.equals("page")) {
            insidePageTag = false;
        }

        if (qName.equals("id")) {
            insideIdTag = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (insideIdTag && documentId == -1) {
            documentId = Long.parseLong(new String(ch, start, length));
        }

        if (insideDocumentTextTag) {
            textBuilder.append(new String(ch, start, length));
        }
    }
}
