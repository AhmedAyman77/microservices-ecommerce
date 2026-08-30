package com.example.orderservice.events;

import com.example.orderservice.models.OrderOutboxEvent;
import com.example.orderservice.repository.OrderOutboxEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderOutboxPublisher {

    @Autowired
    private OrderOutboxEventRepository outboxRepository;

    @Autowired
    private OrderEventProducer orderEventProducer;


    @Scheduled(fixedDelay = 1000)
    public void publishEvents() {

        List<OrderOutboxEvent> events = outboxRepository.findByPublishedFalseOrderByCreatedAtAsc();

        for (OrderOutboxEvent event : events) {
            orderEventProducer.publishOrderPlaced(
                    event.getId(),
                    event.getEmail(),
                    event.getOrderId(),
                    event.getTotalPrice()
            );

            event.setPublished(true);
            event.setPublishedAt(LocalDateTime.now());

            outboxRepository.save(event);
        }
    }
}