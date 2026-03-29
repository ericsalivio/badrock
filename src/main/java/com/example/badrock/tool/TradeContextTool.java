package com.example.badrock.tool;

import com.example.badrock.model.TradeContextRequest;
import com.example.badrock.model.TradeContextResponse;
import com.example.badrock.service.TradeRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TradeContextTool {

    private final TradeRagService ragService;

    @Tool(description = "Retrieve trade lifecycle events using tradeId and question")
    public TradeContextResponse getTradeContext(TradeContextRequest request) {

        List<Document> docs = ragService.searchByTradeId(
                request.tradeId(),
                request.question()
        );

        List<String> events = docs.stream()
                .map(doc -> String.format(
                        "Type=%s | Status=%s | Time=%s | Details=%s",
                        doc.getMetadata().get("type"),
                        doc.getMetadata().get("status"),
                        doc.getMetadata().get("timestamp"),
                        doc.getText()
                ))
                .toList();

        return new TradeContextResponse(events);
    }
}