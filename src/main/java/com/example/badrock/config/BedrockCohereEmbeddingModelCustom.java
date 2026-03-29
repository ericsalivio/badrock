//package com.example.badrock.config;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.ai.embedding.*;
//import software.amazon.awssdk.core.SdkBytes;
//import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
//import software.amazon.awssdk.services.bedrockruntime.model.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class BedrockCohereEmbeddingModelCustom implements EmbeddingModel {
//
//    private final BedrockRuntimeClient client;
//    private final ObjectMapper mapper;
//
//    private static final String MODEL_ID = "cohere.embed-multilingual-v1";
//
//    public BedrockCohereEmbeddingModelCustom(BedrockRuntimeClient client) {
//        this.client = client;
//        this.mapper = new ObjectMapper();
//    }
//
//    @Override
//    public EmbeddingResponse call(EmbeddingRequest request) {
//
//        try {
//            List<String> texts = request.getInstructions();
//
//            String body = mapper.writeValueAsString(
//                    new CohereRequest(texts, "search_document")
//            );
//
//            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
//                    .modelId(MODEL_ID)
//                    .contentType("application/json")
//                    .accept("application/json")
//                    .body(SdkBytes.fromUtf8String(body))
//                    .build();
//
//            InvokeModelResponse response = client.invokeModel(invokeRequest);
//
//            JsonNode root = mapper.readTree(response.body().asUtf8String());
//            JsonNode embeddingsNode = root.get("embeddings");
//
//            List<Embedding> embeddings = new ArrayList<>();
//
//            for (JsonNode node : embeddingsNode) {
//                List<Double> vector = new ArrayList<>();
//                node.forEach(n -> vector.add(n.asDouble()));
//                embeddings.add(new Embedding(vector));
//            }
//
//            return new EmbeddingResponse(embeddings);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Embedding failed", e);
//        }
//    }
//
//    static class CohereRequest {
//        public List<String> texts;
//        public String input_type;
//
//        public CohereRequest(List<String> texts, String inputType) {
//            this.texts = texts;
//            this.input_type = inputType;
//        }
//    }
//}