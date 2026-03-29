package com.example.badrock.controller;

import com.example.badrock.model.TradeAnswer;
import com.example.badrock.service.TradeRagService;
import com.example.badrock.tool.ReplayTradeTool;
import com.example.badrock.tool.TradeContextTool;
import com.example.badrock.tool.TradeLifecycleTool;
import com.example.badrock.tool.TradeTools;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagController {

    private final TradeRagService ragService;
    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final TradeLifecycleTool tradeLifecycleTool;
    private final ReplayTradeTool replayTradeTool;

    @GetMapping("/replayQueue")
    public List<Map<String, Object>> replay(@RequestParam String tradeId) throws Exception {
        List<Document> docs = ragService.search(tradeId,
                List.of("NEWT", "MODI", "EROR", "TERM"),
                "TO_SEND");

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a system that outputs STRICT JSON ONLY.
                Replay the trade lifecycle for trade %s with action types NEWT, MODI, EROR, TERM and status TO_SEND.
                Only include the latest timestamp for each action type.
                Output MUST be an array of objects with fields:
                - actionType
                - tradeId
                - status
                - timestamp
                - details
                Do not include explanations.
                Context:
                %s
                """.formatted(tradeId, context);

        String response = chatClient.prompt().user(prompt).call().content();
        response = extractJson(response);

        try {
            return mapper.readValue(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    @GetMapping("/ask")
    public TradeAnswer query(
            @RequestParam String user,
            @RequestParam String question) {

        // 🔹 Step 1: Get top 3 docs only
        String tradeId = question.replaceAll(".*?(TRX\\d+|T\\d+).*", "$1");

        if (tradeId.equals(question)) {
            // fallback: vector search
            List<String> ids = ragService.searchGlobal(question).stream()
                    .map(d -> String.valueOf(d.getMetadata().get("tradeId")))
                    .toList();

            tradeId = ids.isEmpty() ? "UNKNOWN" : ids.get(0);
        }

        String systemPrompt = """
        You are a Trade Lifecycle Assistant,
        but only respond with trade details if the user explicitly mentions a trade or trade ID.
        If the user asks about themselves or unrelated topics,
        respond naturally and politely without trade info.
        
        Trade Id:  %s
        
        You can use tools:
        - getTradeLifecycleJson(tradeId, lastN)
        - getFailedSteps(tradeId)
        - replayTrade(tradeId, confirmed)
        
        Response rules:
        - Always answer the user clearly
        - Always include a helpful follow-up question
        
        Follow-up rules:
            - If trade FAILED → suggest replay
            - If lifecycle shown → suggest deeper inspection or replay
            - If status → suggest lifecycle
        
  
      
        
        Be concise and natural.
        
        """.formatted(tradeId);

        return chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .tools(replayTradeTool,tradeLifecycleTool)
                .advisors(questionAnswerAdvisor)
                .call()
                .entity(TradeAnswer.class);
    }

//    @GetMapping("/ask")
//    public TradeAnswer query(
//            @RequestParam String user,
//            @RequestParam String question) {
//
//
//        // 🔹 Step 1: Get top 3 docs only
//        String tradeId = question.replaceAll(".*?(TRX\\d+|T\\d+).*", "$1");
//
//        if (tradeId.equals(question)) {
//            // fallback: vector search
//            List<String> ids = ragService.searchGlobal(question).stream()
//                    .map(d -> String.valueOf(d.getMetadata().get("tradeId")))
//                    .toList();
//
//            tradeId = ids.isEmpty() ? "UNKNOWN" : ids.get(0);
//        }
//
//        String systemPrompt = """
//        You are a trade lifecycle assistant.
//        - Answer in short, concise bullet points
//        - Limit total response to ~300 tokens
//        - Always call getTradeLifecycle(tradeId, lastN=50) to fetch lifecycle data
//        - Do NOT return JSON; return plain text
//        - Focus on the user's question
//        """;
//
//        String userPrompt = """
//        User Question: %s
//
//        Trade ID: %s
//
//        Instructions:
//        - Always call getTradeLifecycle(tradeId, lastN=50) to get lifecycle data
//        - Answer the user's question clearly and concisely
//        - Use bullet points, one line per event
//        - Do not include extra text or JSON
//        """.formatted(question, tradeId);
//
//        return chatClient.prompt()
//                .system(systemPrompt)
////                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, user).advisors(questionAnswerAdvisor)
////                )
//                .user(userPrompt)
//                .tools( tradeLifecycleTool)
//                .advisors(questionAnswerAdvisor)
//                .call()
//                .entity(TradeAnswer.class); // 👈 KEY: structured mapping
//    }

    public String extractJson(String raw) {
        int start = raw.indexOf("[");      // start of JSON array
        int end = raw.lastIndexOf("]");    // end of JSON array

        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }

        // fallback: try JSON object
        start = raw.indexOf("{");
        end = raw.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }

        // if nothing found, return empty JSON array
        return "[]";
    }
}