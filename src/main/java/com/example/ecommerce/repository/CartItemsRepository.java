package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.models.CartItems;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItems, UUID> {
    CartItems findByCartId_IdAndProductId_Id(UUID cartId, UUID productId);
    Optional<List<CartItems>> findByCartId_Id(UUID cartId);
    void deleteByCartId_Id(UUID cartId);
}