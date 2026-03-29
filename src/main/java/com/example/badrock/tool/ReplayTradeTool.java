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
public class ReplayTradeTool {

    private final TradeRagService ragService;

    @Tool(name = "replayTrade", description = "Replay a trade lifecycle sequence. Requires confirmation.")
    public String replayTrade(String tradeId, String mode, boolean confirmed) {

        if (!confirmed) {
            return """
                Replay requires confirmation.
                mode:
                - FULL
                - FROM_FAILURE
                - DRY_RUN
            
                Suggested safe option:
                - Run DRY_RUN first
            
                Please confirm to proceed.
            """;
        }

        return "Replayed Mocked";
    }
}