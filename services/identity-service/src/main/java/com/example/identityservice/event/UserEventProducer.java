package com.example.identityservice.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventProducer {
    public static final String TOPIC = "user-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(String email, String token) {
        // key = email, so all events for the same user land on the same
        // Kafka partition and are processed in order
        kafkaTemplate.send(TOPIC, email, new UserRegisteredEvent(email, token));
    }
}