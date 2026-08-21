package com.example.ecommerce.abstracts;

import java.util.List;
import java.util.UUID;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.models.OrderItems;
import com.example.ecommerce.models.Orders;
import org.springframework.security.core.Authentication;

public interface OrderService {
    Orders checkout(Authentication authentication);
    List<Orders> getOrdersByUserId(Authentication authentication);
    List<OrderItems> getOrderItemsByOrderId(UUID orderId);
    List<Orders> getAllOrders();
    Orders updateOrderStatus(UUID orderId, OrderStatus newStatus);
}
