package com.example.badrock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TradeAnswer(
        String tradeId,                    // null if tools not called
        String question,                   // always the original user question
        String answer,                     // populated from tools// free-text for non-trade questions
        String followUpQuestion
) {}