package com.example.ecommerce.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.abstracts.OrderService;
import com.example.ecommerce.dtos.UpdateOrderStatus;
import com.example.ecommerce.models.OrderItems;
import com.example.ecommerce.models.Orders;
import com.example.ecommerce.share.GlobalResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<GlobalResponse<Orders>> checkout(Authentication authentication) {
        Orders order = orderService.checkout(authentication);
        return ResponseEntity.status(201).body(new GlobalResponse<>(order));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<Orders>>> getOrdersByUser(Authentication authentication) {
        List<Orders> orders = orderService.getOrdersByUserId(authentication);
        return ResponseEntity.ok(new GlobalResponse<>(orders));
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<GlobalResponse<List<OrderItems>>> getOrderItems(@PathVariable UUID orderId) {
        List<OrderItems> items = orderService.getOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(new GlobalResponse<>(items));
    }

    @GetMapping("/all")
    public ResponseEntity<GlobalResponse<List<Orders>>> getAllOrders() {
        List<Orders> orders = orderService.getAllOrders();
        return ResponseEntity.ok(new GlobalResponse<>(orders));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<GlobalResponse<Orders>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatus updateOrderStatus
    ) {
        Orders order = orderService.updateOrderStatus(orderId, updateOrderStatus.status());
        return ResponseEntity.ok(new GlobalResponse<>(order));
    }
}