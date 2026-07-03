package com.example.ecommerce.controllers;

import com.example.ecommerce.abstracts.OrderService;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.models.Orders;
import com.example.ecommerce.models.Users;
import com.example.ecommerce.share.CustomException;
import com.example.ecommerce.share.GlobalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrdersController ordersController;

    private UUID userId;
    private Orders order;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        Users user = new Users();
        user.setId(userId);

        order = new Orders();
        order.setId(UUID.randomUUID());
        order.setUserId(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(new BigDecimal("2000.00"));
    }

    @Test
    void checkout_success_returns201() {
        when(orderService.checkout(userId)).thenReturn(order);

        ResponseEntity<GlobalResponse<Orders>> response = ordersController.checkout(userId);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        assertEquals(OrderStatus.PENDING, response.getBody().getData().getStatus());
        assertEquals(new BigDecimal("2000.00"), response.getBody().getData().getTotalPrice());
    }

    @Test
    void checkout_userNotFound_throwsException() {
        when(orderService.checkout(userId))
            .thenThrow(CustomException.resourceNotFound("User not found"));

        CustomException ex = assertThrows(CustomException.class,
            () -> ordersController.checkout(userId));

        assertEquals(404, ex.getStatusCode());
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void checkout_emptyCart_throwsException() {
        when(orderService.checkout(userId))
            .thenThrow(CustomException.badRequest("Cart is empty"));

        CustomException ex = assertThrows(CustomException.class,
            () -> ordersController.checkout(userId));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void checkout_insufficientStock_throwsException() {
        when(orderService.checkout(userId))
            .thenThrow(CustomException.badRequest("Insufficient stock for product: Laptop"));

        CustomException ex = assertThrows(CustomException.class,
            () -> ordersController.checkout(userId));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Insufficient stock for product: Laptop", ex.getMessage());
    }
}
