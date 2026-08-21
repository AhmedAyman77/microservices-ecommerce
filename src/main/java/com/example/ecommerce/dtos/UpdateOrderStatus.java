package com.example.ecommerce.dtos;

import com.example.ecommerce.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatus(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}