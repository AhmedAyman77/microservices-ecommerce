package com.example.notificationservice.config;

import com.example.notificationservice.events.OrderPlacedEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
public class OrderEventConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderPlacedEvent> orderConsumerFactory(
            KafkaProperties kafkaProperties) {

        JacksonJsonDeserializer<OrderPlacedEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderPlacedEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent>
    orderKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderPlacedEvent> orderConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderConsumerFactory);

        return factory;
    }
}