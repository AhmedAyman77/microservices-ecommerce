package com.example.orderservice.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedEvent(
    String email,
    UUID orderId,
    BigDecimal totalPrice
) {}