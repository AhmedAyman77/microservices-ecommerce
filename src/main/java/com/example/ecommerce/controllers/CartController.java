package com.example.ecommerce.controllers;

import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.ecommerce.models.CartItems;

import com.example.ecommerce.abstracts.CartService;
import com.example.ecommerce.dtos.UpdateCartItem;
import com.example.ecommerce.share.GlobalResponse;

import org.springframework.web.bind.annotation.GetMapping;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add/{productId}")
    public ResponseEntity<GlobalResponse<String>> addProductToUserCart(
            Authentication authentication,
            @PathVariable UUID productId
    ) {
        cartService.addProduct(authentication, productId);
        return ResponseEntity.ok(new GlobalResponse<String>("Product added to cart successfully"));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<CartItems>>> getUserCart(Authentication authentication) {
        List<CartItems> cartItems = cartService.getUserCart(authentication);
        return ResponseEntity.ok(new GlobalResponse<List<CartItems>>(cartItems));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<GlobalResponse<String>> removeProductFromUserCart(
            Authentication authentication,
            @PathVariable UUID productId
    ) {
        cartService.removeProduct(authentication, productId);
        return ResponseEntity.ok(new GlobalResponse<String>("Product removed from cart successfully"));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<GlobalResponse<String>> setProductQuantity(
            Authentication authentication,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItem updateCartItem
    ) {
        cartService.setProductQuantity(authentication, productId, updateCartItem.quantity());
        return ResponseEntity.ok(new GlobalResponse<String>("Cart item quantity updated successfully"));
    }

    @DeleteMapping
    public ResponseEntity<GlobalResponse<String>> clearUserCart(Authentication authentication) {
        cartService.clearCart(authentication);
        return ResponseEntity.ok(new GlobalResponse<String>("Cart cleared successfully"));
    }

}