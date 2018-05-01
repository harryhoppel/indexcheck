package com.example;

import com.example.index.IndexDelta;
import com.example.index.InvertedIndex;
import com.example.index.MemoryInvertedIndex;
import com.example.parsing.WikiIntactDocument;
import com.example.parsing.WikiProcessedDocument;
import com.example.parsing.WikiSaxParser;
import org.tartarus.snowball.ext.russianStemmer;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class App {
    private static final int TIMES_TO_CREATE_INDEX = 1;

    private static final int DOCUMENTS_TO_PROCESS = Integer.MAX_VALUE;

    private static final String WIKI_DUMP_PATH
    //        = "main/data/test-simple-docs.txt";
            = "C:\\Users\\vasil_000\\Downloads\\ruwiki-20180401-pages-meta-current1.xml-p4p311181";
    private static final String INDEX_PATH = "C:\\Users\\vasil_000\\Downloads\\index.txt";

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < TIMES_TO_CREATE_INDEX; i++) {
            createIndexFromScratch();
        }
    }

    private static void createIndexFromScratch() throws IOException {
        List<WikiIntactDocument> intactDocuments = new WikiSaxParser().parseDocuments(WIKI_DUMP_PATH);
        AtomicInteger progress = new AtomicInteger(0);
        List<WikiProcessedDocument> processedDocuments = intactDocuments
//                .stream()
                .parallelStream()
                .map(document -> {
                    progress(progress, -1);
                    return new WikiProcessedDocument(document);
                })
                .collect(Collectors.toList());
        System.out.println("Total documents size: " + intactDocuments.size());
        processedDocuments = processedDocuments.subList(0, Math.min(DOCUMENTS_TO_PROCESS, processedDocuments.size()));
        InvertedIndex invertedIndex = new MemoryInvertedIndex();
        int totalDocuments = processedDocuments.size();
        progress.set(0);
        long startTime = System.currentTimeMillis();
        processedDocuments
//                .stream()
                .parallelStream()
                .forEach(doc -> {
                    progress(progress, totalDocuments);
                    russianStemmer russianStemmer = new russianStemmer();
                    IndexDelta delta = new IndexDelta(doc, russianStemmer);
                    invertedIndex.addDelta(delta);
                });
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(MessageFormat.format("Indexing time: {0} ms ({1} sec, {2} min)",
                totalTime, totalTime / 1000, totalTime / 1000 / 60));
        invertedIndex.dumpToDisk(INDEX_PATH);
        long timeToDumpIndex = System.currentTimeMillis() - startTime - totalTime;
        System.out.println(MessageFormat.format("Time to dump index to disk: {0} ms ({1} sec; {2} min)",
                timeToDumpIndex, timeToDumpIndex / 1000, timeToDumpIndex / 1000 / 60));
    }

    private static void progress(AtomicInteger progress, int total) {
        int currentNumber = progress.incrementAndGet();
        if (currentNumber % 10000 == 0) {
            if (total == -1) {
                System.out.println("Processed " + currentNumber + " items");
            } else {
                System.out.println("Processed " + (currentNumber * 100 / total) + "%");
            }
        }
    }
}
