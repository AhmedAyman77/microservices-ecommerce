package com.example.notificationservice.events;

import com.example.notificationservice.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "order-events", containerFactory = "orderKafkaListenerContainerFactory")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("Received OrderPlacedEvent for order: " + event.orderId());
        emailService.orderConfirmationEmail(event.email(), event.orderId().toString(), event.totalPrice());
    }
}