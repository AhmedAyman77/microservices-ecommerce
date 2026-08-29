package com.example.cartservice.abstracts;

import com.example.cartservice.models.CartItems;

import java.util.List;
import java.util.UUID;

public interface CartService {
    public void addProduct(UUID userId, UUID productId);

    public void removeProduct(UUID userId, UUID productId);

    public void setProductQuantity(UUID userId, UUID productId, Integer quantity);

    public void clearCart(UUID userId);

    public List<CartItems> getUserCart(UUID userId);
}