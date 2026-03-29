//package com.example.badrock.service;
//
//import com.example.badrock.model.TradeAnswer;
//import com.example.badrock.tool.TradeLifecycleTool;
//import com.example.badrock.tool.TradeTools;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class TradeAgentService {
//
//    private final ChatClient chatClient;
//    private final TradeTools tradeIdTool;
//    private final TradeLifecycleTool lifecycleTool;
//    private final ObjectMapper objectMapper;
//
//    public TradeAnswer ask(String user, String question) throws Exception {
//
//        boolean isTrade = isTradeQuestion(question);
//
//        // =========================
//        // NON-TRADE FLOW (NO MEMORY)
//        // =========================
//        if (!isTrade) {
//
//            String response = chatClient.prompt()
//                    .user(question)
//                    .call()
//                    .content();
//
//            return new TradeAnswer(
//                    null,
//                    question,
//                    List.of(),
//                    "tools not invoked",
//                    response
//            );
//        }
//
//        // =========================
//        // TRADE FLOW (TOOLS ONLY)
//        // =========================
//        String systemPrompt = """
//            You are a trade lifecycle AI assistant.
//
//            Instructions:
//            1. Extract tradeId from the question
//            2. Call getTradeLifecycle(tradeId, lastN=10)
//            3. Return ONLY tool result
//
//            STRICT:
//            - JSON ONLY
//            - NO explanation
//            - NO thinking
//            - DO NOT hallucinate
//        """;
//
//        var response = chatClient.prompt()
//                .system(systemPrompt)
//                .user(question)
//                .tools(tradeIdTool, lifecycleTool)
//                .call();
//
//        String raw = response.getOutput().getMessage().getContent()
//                .stream()
//                .map(c -> c.getText())
//                .reduce("", String::concat)
//                .trim();
//
//        // remove ```json if exists
//        raw = raw.replaceAll("(?s)^```json|```$", "").trim();
//
//        TradeAnswer result = objectMapper.readValue(raw, TradeAnswer.class);
//
//        // FINAL GUARD
//        if (result.tradeId() == null) {
//            return new TradeAnswer(
//                    null,
//                    question,
//                    List.of(),
//                    "tools not invoked",
//                    "Trade ID not found."
//            );
//        }
//
//        return result;
//    }
//
//    private boolean isTradeQuestion(String question) {
//        return question.matches(".*(TRX\\d+|\\b\\d{5}\\b|trade|status|lifecycle).*");
//    }
//}