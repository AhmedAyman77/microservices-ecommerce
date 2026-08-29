package com.example.cartservice.repository;

import com.example.cartservice.models.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItems, UUID> {
    CartItems findByCartId_IdAndProductId(UUID cartId, UUID productId);
    Optional<List<CartItems>> findByCartId_Id(UUID cartId);
    void deleteByCartId_Id(UUID cartId);
}