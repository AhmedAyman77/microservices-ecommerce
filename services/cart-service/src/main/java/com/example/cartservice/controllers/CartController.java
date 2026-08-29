package com.example.cartservice.controllers;

import com.example.cartservice.abstracts.CartService;
import com.example.cartservice.dtos.UpdateCartItem;
import com.example.cartservice.models.CartItems;
import com.example.cartservice.share.GlobalResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    private UUID getUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<GlobalResponse<String>> addProductToUserCart(
            Authentication authentication,
            @PathVariable UUID productId
    ) {
        cartService.addProduct(getUserId(authentication), productId);
        return ResponseEntity.ok(new GlobalResponse<String>("Product added to cart successfully"));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<GlobalResponse<String>> removeProductFromUserCart(
            Authentication authentication,
            @PathVariable UUID productId
    ) {
        cartService.removeProduct(getUserId(authentication), productId);
        return ResponseEntity.ok(new GlobalResponse<String>("Product removed from cart successfully"));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<GlobalResponse<String>> setProductQuantity(
            Authentication authentication,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItem updateCartItem
    ) {
        cartService.setProductQuantity(getUserId(authentication), productId, updateCartItem.quantity());
        return ResponseEntity.ok(new GlobalResponse<String>("Cart item quantity updated successfully"));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<CartItems>>> getUserCart(Authentication authentication) {
        List<CartItems> cartItems = cartService.getUserCart(getUserId(authentication));
        return ResponseEntity.ok(new GlobalResponse<List<CartItems>>(cartItems));
    }

    @DeleteMapping
    public ResponseEntity<GlobalResponse<String>> clearUserCart(Authentication authentication) {
        cartService.clearCart(getUserId(authentication));
        return ResponseEntity.ok(new GlobalResponse<String>("Cart cleared successfully"));
    }

}