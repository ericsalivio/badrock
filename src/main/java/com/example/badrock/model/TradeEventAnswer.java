package com.example.badrock.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TradeEventAnswer(
        String type,
        String status,
        String timestamp,
        String details,
        String text) {}