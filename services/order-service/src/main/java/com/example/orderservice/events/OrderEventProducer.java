package com.example.orderservice.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class OrderEventProducer {
    public static final String TOPIC = "order-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderPlaced(String email, UUID orderId, BigDecimal totalPrice) {
        // key = orderId, so events for the same order stay ordered
        kafkaTemplate.send(TOPIC, orderId.toString(), new OrderPlacedEvent(email, orderId, totalPrice));
    }
}