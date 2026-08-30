package com.example.notificationservice.events;

import com.example.notificationservice.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "user-events")
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("Received UserRegisteredEvent for: " + event.email());
        emailService.verifyAccountCreationEmail(event.email(), event.token());
    }
}