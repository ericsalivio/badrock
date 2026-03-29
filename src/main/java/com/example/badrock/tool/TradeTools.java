package com.example.badrock.tool;

import com.example.badrock.model.TradeIdRequest;
import com.example.badrock.model.TradeIdResponse;
import com.example.badrock.service.TradeRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TradeTools {

    private final TradeRagService ragService;

    @Tool(description = "Extract or find the tradeId from a user question")
    public TradeIdResponse findTradeId(TradeIdRequest request) {

        String question = request.question();

        // Try regex first (fast)
        String tradeId = question.replaceAll(".*?(TRX\\d+|T\\d+).*", "$1");

        if (tradeId.equals(question)) {
            // fallback: vector search
            List<String> ids = ragService.searchGlobal(question).stream()
                    .map(d -> String.valueOf(d.getMetadata().get("tradeId")))
                    .toList();

            tradeId = ids.isEmpty() ? "UNKNOWN" : ids.get(0);
        }

        return new TradeIdResponse(tradeId);
    }
}