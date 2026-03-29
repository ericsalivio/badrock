package com.example.badrock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplayRequest(
        String tradeId,                    // null if tools not called
        String mode,                   // always the original user question
        boolean confirmed
) {}