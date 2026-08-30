package com.example.notificationservice.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedEvent(
    String email,
    UUID orderId,
    BigDecimal totalPrice
) {}