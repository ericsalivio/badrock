package com.example.badrock.service;


import com.example.badrock.model.TradeEvent;
import com.example.badrock.repository.TradeEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeEventRepository repo;
    private final ApplicationEventPublisher publisher;

    public void createEvent(String tradeId, String status, String type, String product, String extractionType) {

        TradeEvent event = TradeEvent.builder()
                .tradeId(tradeId)
                .status(status)
                .actionType(type)
                .product(product)
                .extractionType(extractionType)
                .build();

        repo.save(event);
        publisher.publishEvent(event);
    }
}