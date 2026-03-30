package com.example.badrock.controller;

import com.example.badrock.model.ChatForm;
import com.example.badrock.model.ChatMessage;
import com.example.badrock.model.TradeAnswer;
import com.example.badrock.service.TradeRagService;
import com.example.badrock.tool.ReplayTradeTool;
import com.example.badrock.tool.TradeLifecycleTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final TradeRagService ragService;
    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final PromptChatMemoryAdvisor promptChatMemoryAdvisor;
    private final TradeLifecycleTool tradeLifecycleTool;
    private final ReplayTradeTool replayTradeTool;

    @GetMapping
    public String getChatPage(ChatForm chatForm, Model model) {
        ChatMessage newMessage = new ChatMessage(chatForm.getUsername(), chatForm.getMessageText());
        newMessage.setUsername(chatForm.getUsername());
        model.addAttribute("chatMessages", newMessage);
        return "chat";
    }
//
//    // Load chat page
//    @GetMapping
//    public String chatPage(HttpSession session, Model model) {
//        List<ChatMessage> chatMessages = (List<ChatMessage>) session.getAttribute("chatMessages");
//        if (chatMessages == null) {
//            chatMessages = new ArrayList<>();
//            session.setAttribute("chatMessages", chatMessages);
//        }
//        model.addAttribute("chatMessages", chatMessages);
//        return "chat";
//    }

    @PostMapping
    public String sendMessage(
            ChatForm chatForm, Model model, Principal principal, HttpSession session
    ) {
        // Get existing chat messages from session or create new list
        List<ChatMessage> chatMessages = (List<ChatMessage>) session.getAttribute("chatMessages");
        if (chatMessages == null) {
            chatMessages = new ArrayList<>();
        }

        // Add user message
        chatMessages.add(new ChatMessage(principal.getName(), chatForm.getMessageText()));
        String question = chatForm.getMessageText();

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

        var response = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .tools(replayTradeTool, tradeLifecycleTool)
                .advisors(questionAnswerAdvisor)
                .call()
                .entity(TradeAnswer.class);

        chatMessages.add(new ChatMessage("AI", response.answer()));           // main AI answer
        chatMessages.add(new ChatMessage("AI", response.followUpQuestion())); // follow-up question

        // Save updated chat history in session
        session.setAttribute("chatMessages", chatMessages);
        model.addAttribute("chatMessages", chatMessages);
        // Clear input
        chatForm.setMessageText("");
        return "chat";
    }

    // Streaming endpoint for EventSource (GET for native EventSource)
    @GetMapping("/stream-sse")
    public SseEmitter streamMessage(@RequestParam String message, Principal principal) {
        SseEmitter emitter = new SseEmitter(0L);
        List<ChatMessage> chatHistory = new ArrayList<>();// no timeout
        new Thread(() -> {
            try {
                String username = principal != null ? principal.getName() : "Guest";

                // 1️⃣ Send user message
                ChatMessage userMsg = new ChatMessage("Thinking..", message);
                chatHistory.add(userMsg);
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(userMsg));

                String question = message;

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

                var response = chatClient.prompt()
                        .system(systemPrompt)
                        .user(question)
                        .tools(replayTradeTool, tradeLifecycleTool)
                        .advisors(questionAnswerAdvisor)
                        .call()
                        .entity(TradeAnswer.class);

                ChatMessage aiMsg = new ChatMessage("", "");
                chatHistory.add(aiMsg);
                // 2️⃣ Simulate AI thinking & send character-by-character
                String fullText = response.answer() + "\n Follow-up: " + response.followUpQuestion();

                for (char c : fullText.toCharArray()) {
                    aiMsg.setMessageText(aiMsg.getMessageText() + c);
                    emitter.send(SseEmitter.event().name("message").data(aiMsg));
                    Thread.sleep(20); // simulate typing
                }

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    @ModelAttribute("allMessageTypes")
    public String[] allMessageTypes() {
        return new String[]{"Say", "Shout", "Whisper"};
    }

}
