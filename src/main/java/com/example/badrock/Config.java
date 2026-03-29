//package com.example.badrock;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//@Configuration
//public class Config {
//
//    @Bean
//    public VectorStore vectorStore(JdbcTemplate jdbcTemplate,
//                                   EmbeddingClient embeddingClient) {
//
//        return PgVectorStore.builder(jdbcTemplate, embeddingClient)
//                .dimensions(1024) // Cohere embedding size
//                .build();
//    }
//
//    @Bean
//    public EmbeddingClient embeddingClient(BedrockEmbeddingClient client) {
//        return request -> client.embedding()
//                .model("cohere.embed-english-v3")
//                .input(request.getInput())
//                .call();
//    }
//
//    @Bean
//    public ChatClient chatClient(BedrockChatClient client) {
//        return ChatClient.create(client)
//                .defaultOptions(options -> options.withModel("amazon.nova-2-lite"));
//    }
//}