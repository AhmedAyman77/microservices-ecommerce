package com.example.orderservice.dtos;

import java.util.UUID;

public record CartItemsResponse(
        UUID cartId,
        UUID productId,
        Integer quantity
) {}
