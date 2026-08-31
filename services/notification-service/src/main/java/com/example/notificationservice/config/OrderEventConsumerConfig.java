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

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OrderEventConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderPlacedEvent> orderConsumerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.remove("spring.json.value.default.type");
        props.remove("spring.json.trusted.packages");
        props.remove("spring.json.use.type.headers");

        props.put("group.id", "notification-service-order-events");

        JacksonJsonDeserializer<OrderPlacedEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderPlacedEvent.class);
        deserializer.setUseTypeHeaders(false); // always use OrderPlacedEvent, ignore any type header from the producer
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                props,
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