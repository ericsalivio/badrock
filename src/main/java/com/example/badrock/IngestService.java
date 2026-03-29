package com.example.badrock;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestService {

    private final VectorStore vectorStore;


    public IngestService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;

    }

    public void ingest(String text) {

        vectorStore.add(List.of(new Document(text)));
    }
}