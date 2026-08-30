package com.example.orderservice.dtos;

import com.example.orderservice.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatus(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}