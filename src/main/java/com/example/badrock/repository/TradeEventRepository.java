package com.example.badrock.repository;


import com.example.badrock.model.TradeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeEventRepository extends JpaRepository<TradeEvent, Long> {
}