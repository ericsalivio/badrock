package com.example.badrock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;



@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeEvent {

    @Id
    @GeneratedValue
    private Long id;

    private String tradeId;
    private String status; // NEW, VALIDATED, FAILED, RETRY, SETTLED
    private String actionType;
    private String product;
    private String extractionType;
    private String details;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // getters & setters
}