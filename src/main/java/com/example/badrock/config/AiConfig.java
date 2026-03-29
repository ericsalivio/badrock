package com.example.badrock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import javax.sql.DataSource;


@Configuration
public class AiConfig {



    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, QuestionAnswerAdvisor questionAnswerAdvisor) {
        return builder.defaultAdvisors(questionAnswerAdvisor).build();
    }

    @Bean
    public PromptChatMemoryAdvisor promptChatMemoryAdvisor(DataSource dataSource) {
       var jdbc= JdbcChatMemoryRepository.builder().dataSource(dataSource).build();
       var mwa = MessageWindowChatMemory.builder().maxMessages(3).chatMemoryRepository(jdbc).build();
       return PromptChatMemoryAdvisor.builder(mwa
       ).build();
    }

    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
//        var cohereEmbeddingApi =new CohereEmbeddingBedrockApi(
//                CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V1.id(),
//                EnvironmentVariableCredentialsProvider.create(), Region.US_EAST_1.id(), new JsonMapper());
//
//
//        var embeddingModel = new BedrockCohereEmbeddingModel(this.cohereEmbeddingApi);
//
//        EmbeddingResponse embeddingResponse = this.embeddingModel
//                .embedForResponse(List.of("Hello World", "World is big and salvation is near"));
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }
}