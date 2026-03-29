package com.example.badrock.service;


import com.example.badrock.repository.TradeEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {


    private final TradeService tradeService;
    private final TradeEventRepository tradeRepo;
    private final VectorStore vectorStore;

    @Override
    public void run(String... args) {

        tradeRepo.deleteAll();

        tradeService.createEvent("12345", "TO_SEND", "NEW", "Trade created");
        tradeService.createEvent("12345", "TO_SEND", "MODI", "Risk passed");
        tradeService.createEvent("12345", "ACK", "EROR", "Deadlock");
        tradeService.createEvent("12345", "FILTERED", "TERM", "Retrying");
        tradeService.createEvent("12345", "NACK", "RECYCLED", "Success");



        System.out.println("Trade events inserted at startup");
    }
}