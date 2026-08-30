package com.example.orderservice.abstracts;

import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.models.OrderItems;
import com.example.orderservice.models.Orders;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    Orders checkout(UUID userId);
    List<Orders> getOrdersByUserId(UUID userId);
    List<OrderItems> getOrderItemsByOrderId(UUID orderId);
    List<Orders> getAllOrders();
    Orders updateOrderStatus(UUID orderId, OrderStatus newStatus);
}
