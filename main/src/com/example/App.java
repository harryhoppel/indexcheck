package com.example;

import com.example.parsing.WikiDocument;
import com.example.parsing.WikiParser;
import com.example.parsing.WikiSaxParser;

import java.util.List;

public class App {
    public static final String WIKI_DUMP_PATH
            = "C:\\Users\\vasily\\Downloads" +
            "\\ruwiki-20180401-pages-meta-current3.xml-p2953602p3081524" +
            "\\ruwiki-20180401-pages-meta-current3.xml-p2953602p3081524";

    public static void main(String[] args) {
        WikiParser parser = new WikiSaxParser();
        List<WikiDocument> wikiDocuments = parser.parseDocuments(WIKI_DUMP_PATH);

    }
}
