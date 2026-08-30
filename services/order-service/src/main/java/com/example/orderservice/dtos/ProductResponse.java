package com.example.orderservice.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        String name,
        Integer quantity,
        BigDecimal price,
        UUID category
) {}
