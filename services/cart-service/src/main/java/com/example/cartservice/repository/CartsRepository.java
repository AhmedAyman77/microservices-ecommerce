package com.example.cartservice.repository;

import com.example.cartservice.models.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartsRepository extends JpaRepository<Carts, UUID> {
    Optional<Carts> findByUserId(UUID userId);
}
