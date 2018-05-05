package com.example;

import com.example.index.IndexDelta;
import com.example.index.InvertedIndex;
import com.example.index.MemoryInvertedIndex;
import com.example.index.WordPosition;
import com.example.parsing.WikiIntactDocument;
import com.example.parsing.WikiProcessedDocument;
import com.example.parsing.WikiSaxParser;
import javafx.util.Pair;
import org.tartarus.snowball.ext.russianStemmer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class App {
    private static final int TIMES_TO_CREATE_INDEX = 1;

    private static final int DOCUMENTS_TO_PROCESS = Integer.MAX_VALUE;

    private static final String WIKI_DUMP_PATH
    //        = "main/data/test-simple-docs.txt";
            = "Downloads\\ruwiki-20180401-pages-meta-current1.xml-p4p311181";
    private static final String INDEX_PATH = "Downloads\\index.txt";
    private static final String DUMP_FILE_PATH = "Downloads\\dump.txt";
    private static final String RECREATED_DUMP = "Downloads\\recreated_dump.txt";

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < TIMES_TO_CREATE_INDEX; i++) {
            createIndexFromScratch();
        }
    }

    private static void createIndexFromScratch() throws IOException {
        List<WikiIntactDocument> intactDocuments = new WikiSaxParser().parseDocuments(
                new File(System.getProperty("user.home"), WIKI_DUMP_PATH));
        System.out.println("Total documents parsed from dump: " + intactDocuments.size());
        intactDocuments = intactDocuments.subList(0, Math.min(DOCUMENTS_TO_PROCESS, intactDocuments.size()));
        System.out.println("Documents to create index: " + intactDocuments.size());

        AtomicInteger progress = new AtomicInteger(0);
        List<WikiProcessedDocument> processedDocuments = convertToProcessedDocuments(intactDocuments, progress);
        calculateTotalTextSize(processedDocuments);
        dumpProcessedDocuments(processedDocuments);

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

        invertedIndex.dumpToDisk(new File(System.getProperty("user.home"), INDEX_PATH));
        long timeToDumpIndex = System.currentTimeMillis() - startTime - totalTime;
        System.out.println(MessageFormat.format("Time to dump index to disk: {0} ms ({1} sec; {2} min)",
                timeToDumpIndex, timeToDumpIndex / 1000, timeToDumpIndex / 1000 / 60));

        dumpRecreatedDocumentsFromIndex(invertedIndex);
    }

    private static void dumpRecreatedDocumentsFromIndex(InvertedIndex invertedIndex) throws IOException {
        long startTime = System.currentTimeMillis();
        Map<Long, List<Pair<Integer, String>>> recreatedDocuments = new HashMap<>();
        Set<String> allIndexedWords = invertedIndex.getAllWords();
        for (String word : allIndexedWords) {
            SortedSet<WordPosition> wordPositions = invertedIndex.getWordPositions(word);
            for (WordPosition wordPosition : wordPositions) {
                List<Pair<Integer, String>> positionsAndWords = recreatedDocuments.computeIfAbsent(wordPosition.getDocumentId(), i -> new ArrayList<>());
                positionsAndWords.add(new Pair<>(wordPosition.getDocumentPosition(), word));
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(
                new File(System.getProperty("user.home"), RECREATED_DUMP)))) {
            List<Long> docIds = new ArrayList<>(recreatedDocuments.keySet());
            docIds.sort(Long::compareTo);
            for (Long docId : docIds) {
                writer.write("" + docId + "\t");
                List<Pair<Integer, String>> positionsAndWords = recreatedDocuments.get(docId);
                positionsAndWords.sort(Comparator.comparingInt(Pair::getKey));
                StringBuilder textBuilder = new StringBuilder();
                for (Pair<Integer, String> positionAndWord : positionsAndWords) {
                    textBuilder.append(positionAndWord.getValue()).append(' ');
                }
                if (textBuilder.length() > 0) {
                    textBuilder.deleteCharAt(textBuilder.length() - 1);
                }
                textBuilder.append("\n");
                writer.write(textBuilder.toString());
            }
        }
        System.out.println("Total time to recreate all documents: " + (System.currentTimeMillis() - startTime));
    }

    private static List<WikiProcessedDocument> convertToProcessedDocuments(List<WikiIntactDocument> intactDocuments, AtomicInteger progress) {
        return intactDocuments
//                .stream()
                .parallelStream()
                .map(document -> {
                    progress(progress, -1);
                    return new WikiProcessedDocument(document);
                })
                .collect(Collectors.toList());
    }

    private static void dumpProcessedDocuments(List<WikiProcessedDocument> processedDocuments) throws IOException {
        File dumpFile = new File(System.getProperty("user.home"), DUMP_FILE_PATH);
        if (!dumpFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dumpFile))) {
                processedDocuments
                        .forEach(pd -> {
                            try {
                                String processedText = pd.getProcessedText();
                                StringBuilder processedStemmedText = new StringBuilder();
                                russianStemmer russianStemmer = new russianStemmer();
                                for (String word : processedText.split(" ")) {
                                    russianStemmer.setCurrent(word);
                                    russianStemmer.stem();
                                    processedStemmedText.append(russianStemmer.getCurrent()).append(' ');
                                }
                                if (processedStemmedText.length() > 0) {
                                    processedStemmedText.deleteCharAt(processedStemmedText.length() - 1);
                                }
                                writer.write("" + pd.getId() + "\t" + processedStemmedText + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }

    private static void calculateTotalTextSize(List<WikiProcessedDocument> processedDocuments) {
        int totalTextLength = processedDocuments
                .parallelStream()
                .map(pd -> pd.getProcessedText().length())
                .reduce((a, b) -> a + b)
                .orElse(-1);
        System.out.println("Total number of all characters in all documents to process: " + totalTextLength);
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
