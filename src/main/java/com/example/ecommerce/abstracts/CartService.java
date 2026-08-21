package com.example.ecommerce.abstracts;

import java.util.List;
import java.util.UUID;

import com.example.ecommerce.models.CartItems;
import org.springframework.security.core.Authentication;

public interface CartService {
    public void addProduct(Authentication authentication, UUID productId);

    public void removeProduct(Authentication authentication, UUID productId);

    public void setProductQuantity(Authentication authentication, UUID productId, Integer quantity);

    public void clearCart(Authentication authentication);

    public List<CartItems> getUserCart(Authentication authentication);
}