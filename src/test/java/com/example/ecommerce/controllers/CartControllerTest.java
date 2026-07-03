package com.example.ecommerce.controllers;

import com.example.ecommerce.abstracts.CartService;
import com.example.ecommerce.share.CustomException;
import com.example.ecommerce.share.GlobalResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @Test
    void addProduct_success_returns200() {
        doNothing().when(cartService).addProduct(userId, productId);

        ResponseEntity<GlobalResponse<String>> response = cartController.addProductToUserCart(userId, productId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Product added to cart successfully", response.getBody().getData());
    }

    @Test
    void addProduct_serviceThrows_propagatesException() {
        doThrow(CustomException.internalServerError("Error occurred while adding product to cart"))
            .when(cartService).addProduct(userId, productId);

        CustomException ex = assertThrows(CustomException.class,
            () -> cartController.addProductToUserCart(userId, productId));

        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void removeProduct_success_returns200() {
        doNothing().when(cartService).removeProduct(userId, productId);

        ResponseEntity<GlobalResponse<String>> response = cartController.removeProductFromUserCart(userId, productId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Product removed from cart successfully", response.getBody().getData());
    }

    @Test
    void removeProduct_serviceThrows_propagatesException() {
        doThrow(CustomException.internalServerError("Error occurred while removing product from cart"))
            .when(cartService).removeProduct(userId, productId);

        CustomException ex = assertThrows(CustomException.class,
            () -> cartController.removeProductFromUserCart(userId, productId));

        assertEquals(500, ex.getStatusCode());
    }
}
