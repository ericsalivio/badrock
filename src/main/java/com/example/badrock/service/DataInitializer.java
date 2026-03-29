package com.example.badrock.service;


import com.example.badrock.repository.TradeEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {


    private final TradeService tradeService;
    private final TradeEventRepository tradeRepo;
    private final VectorStore vectorStore;
    private static final Map<String, String> ACTION_MEANING = Map.of(
            "NEWT", "New trade reported for the first time",
            "MODI", "Modification of an existing trade",
            "CORR", "Correction of previously reported trade data",
            "TERM", "Termination of an existing trade",
            "EROR", "Cancellation of an incorrect or duplicate trade",
            "REVI", "Revival of a previously terminated or cancelled trade",
            "VALU", "Valuation update of a trade",
            "MARU", "Margin or collateral update"
    );
    @Override
    public void run(String... args) {

        tradeRepo.deleteAll();

        tradeService.createEvent("12345", "TO_SEND", "NEWT","SWAP","REAL_TIME");
        tradeService.createEvent("12345", "TO_SEND", "MODI","SWAP","REAL_TIME");
        tradeService.createEvent("12345", "TO_SEND", "EROR","SWAP","REAL_TIME");
        tradeService.createEvent("12345", "TO_SEND", "EROR","FXSWAP","REAL_TIME");

        System.out.println("Trade events inserted at startup");
    }
}