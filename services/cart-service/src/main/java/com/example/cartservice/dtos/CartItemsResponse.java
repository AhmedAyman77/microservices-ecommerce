package com.example.cartservice.dtos;

import com.example.cartservice.models.Carts;
import java.util.UUID;

public record CartItemsResponse(
        UUID cartId,
        UUID productId,
        Integer quantity
) {}
