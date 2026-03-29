//package com.example.badrock.advisor;
//
//import org.springframework.ai.chat.client.advisor.Advisor;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Component
//public class TradeAdvisor implements ChatAdvisor {
//
//    private final TradeLifecycleTool tradeLifecycleTool;
//
//    public TradeAdvisor(TradeLifecycleTool tradeLifecycleTool) {
//        this.tradeLifecycleTool = tradeLifecycleTool;
//    }
//
//    @Override
//    public String getContext(String userMessage) {
//        // Extract tradeId from user question
//        String tradeId = userMessage.replaceAll("\\D+", "");
//        if (tradeId.isEmpty()) {
//            return "No trade ID found in user question.";
//        }
//
//        // Get only TO_SEND events
//        List<Map<String, String>> events = tradeLifecycleTool.getToSendEvents(tradeId);
//        if (events.isEmpty()) {
//            return "Trade " + tradeId + " has no TO_SEND events.";
//        }
//
//        // Build concise bullet points for LLM
//        return events.stream()
//                .map(e -> String.format("- %s at %s: %s",
//                        e.get("status"), e.get("ts"), e.get("info")))
//                .collect(Collectors.joining("\n"));
//    }
//}