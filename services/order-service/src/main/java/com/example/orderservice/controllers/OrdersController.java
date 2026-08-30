package com.example.orderservice.controllers;

import com.example.orderservice.abstracts.OrderService;
import com.example.orderservice.dtos.UpdateOrderStatus;
import com.example.orderservice.models.OrderItems;
import com.example.orderservice.models.Orders;
import com.example.orderservice.share.GlobalResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    private UUID getUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/checkout")
    public ResponseEntity<GlobalResponse<Orders>> checkout(Authentication authentication) {
        Orders order = orderService.checkout(getUserId(authentication));
        return ResponseEntity.status(201).body(new GlobalResponse<>(order));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<Orders>>> getOrdersByUser(Authentication authentication) {
        List<Orders> orders = orderService.getOrdersByUserId(getUserId(authentication));
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