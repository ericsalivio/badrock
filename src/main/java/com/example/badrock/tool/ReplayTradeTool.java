package com.example.badrock.tool;

import com.example.badrock.service.TradeRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplayTradeTool {

    private final TradeRagService ragService;

    @Tool(
            name = "replayTrade",
            description = """
          Replay the latest trade lifecycle events automatically.
          Parameters:
         - tradeId (string): Trade ID to replay
    """
    )
    public String replayTrade(String tradeId, boolean confirmed) {

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