package com.example.orderservice.repository;

import com.example.orderservice.models.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, UUID> {
    List<OrderItems> findByOrderId_Id(UUID orderId);
}
