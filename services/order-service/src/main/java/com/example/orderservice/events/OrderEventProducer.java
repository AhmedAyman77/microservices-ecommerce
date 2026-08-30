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

    public void publishOrderPlaced(UUID eventId, String email, UUID orderId, BigDecimal totalPrice) {
        try {
            kafkaTemplate
                    .send(TOPIC, orderId.toString(), new OrderPlacedEvent(email, orderId, totalPrice))
                    .get();
        } catch (Exception e) {
            System.err.println(
                    "Failed to publish order event: "
                            + eventId
            );
        }
    }
}