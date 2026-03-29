package com.example.badrock.service;


import com.example.badrock.model.TradeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeRagService {

    private final VectorStore vectorStore;
    // Ingest event to VectorStore with rich embedding
    public void ingest(TradeEvent event){
        String text = String.format(
                "TradeId:%s Type:%s Status:%s Details:%s Timestamp:%s",
                event.getTradeId(),
                event.getType(),
                event.getStatus(),
                event.getDetails(),
                event.getTimestamp()
        );

        Map<String,Object> metadata = Map.of(
                "tradeId", event.getTradeId(),
                "type", event.getType(),
                "status", event.getStatus(),
                "timestamp", event.getTimestamp().toString()
        );

        vectorStore.add(List.of(new Document(text, metadata)));
    }

    // Search with metadata filtering, return latest per action type
    public List<Document> search(String tradeId, List<String> actionTypes, String status){
        // Ingest event to VectorStore with rich embedding
        SearchRequest req = SearchRequest.builder()
                .query("Replay trade lifecycle")
                .topK(100)
                .build();

        List<Document> docs = vectorStore.similaritySearch(req);

        // Filter in Java
        List<Document> filtered = docs.stream()
                .filter(d -> tradeId.equals(d.getMetadata().get("tradeId")))
                .filter(d -> status.equals(d.getMetadata().get("status")))
                .filter(d -> actionTypes.contains(d.getMetadata().get("type")))
                .collect(Collectors.toList());

        // Deduplicate by latest timestamp
        Map<String, Document> latestByAction = filtered.stream()
                .collect(Collectors.toMap(
                        d -> d.getMetadata().get("type").toString(),
                        Function.identity(),
                        (d1,d2) -> {
                            LocalDateTime t1 = LocalDateTime.parse((CharSequence) d1.getMetadata().get("timestamp"));
                            LocalDateTime t2 = LocalDateTime.parse((CharSequence) d2.getMetadata().get("timestamp"));
                            return t1.isAfter(t2) ? d1 : d2;
                        }
                ));

        return latestByAction.values().stream()
                .sorted(Comparator.comparing(d -> LocalDateTime.parse((CharSequence) d.getMetadata().get("timestamp"))))
                .collect(Collectors.toList());


    }
    public List<Document> searchByTradeId(String tradeId, String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query + " for trade " + tradeId)
                .topK(5)
                .filterExpression("tradeId == '" + tradeId + "'")
                .build();

        return vectorStore.similaritySearch(request);
    }

    public List<Document> searchGlobal(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(5)
                .build();

        return vectorStore.similaritySearch(request);
    }
    public List<Document> search(String query){
        // Ingest event to VectorStore with rich embedding
        SearchRequest req = SearchRequest.builder()
                .query(query)
                .topK(100)
                .build();

        return vectorStore.similaritySearch(req);

    }

    public List<Document> search(String tradeId,String query){
        // Ingest event to VectorStore with rich embedding
        SearchRequest req = SearchRequest.builder()
                .query(query)
                .topK(100)
                .build();

        return vectorStore.similaritySearch(req);

    }

}