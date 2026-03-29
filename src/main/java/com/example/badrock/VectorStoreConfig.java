//package com.example.badrock;
//
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//@Configuration
//public class VectorStoreConfig {
//
//    @Bean
//    public VectorStore vectorStore(JdbcTemplate jdbcTemplate,
//                                   EmbeddingClient embeddingClient) {
//        // Use Spring AI API for pgvector
//        return PgVectorStore.builder(jdbcTemplate, embeddingClient)
//                .tableName("documents")
//                .dimensions(1024) // match Cohere embeddings
//                .build();
//    }
//}