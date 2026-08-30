package com.example.orderservice.repository;

import com.example.orderservice.models.OrderOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderOutboxEventRepository
        extends JpaRepository<OrderOutboxEvent, UUID> {

    List<OrderOutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}