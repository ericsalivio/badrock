package com.example.badrock.tool;

import com.example.badrock.model.TradeEventAnswer;
import com.example.badrock.service.TradeRagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TradeLifecycleTool {

    private final TradeRagService ragService;

    private final ObjectMapper objectMapper; // Jackson mapper

    @Tool(description = "Returns latest N trade lifecycle events for a tradeId")
    public  String getTradeLifecycleJson(String tradeId, int lastN) {

        List<Document> docs = ragService.searchByTradeId(tradeId, "full trade lifecycle");

        // Sort by timestamp descending, pick last N
        return docs.stream()
                .sorted((d1,d2) -> ((String)d2.getMetadata().get("timestamp"))
                        .compareTo((String)d1.getMetadata().get("timestamp")))
                .limit(lastN)
                .map(doc -> {
                    Map<String, Object> m = doc.getMetadata();
                    return String.format(
                            "{\"status\":\"%s\",\"ts\":\"%s\"}",
                            m.get("status"),
                            m.get("timestamp")
                    );
                })
                .distinct()
                .collect(Collectors.joining(","));
    }
}