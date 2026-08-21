package com.example.ecommerce.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.models.Products;

@Repository
public interface ProductRepository extends JpaRepository<Products, UUID> {
    boolean existsByCategory_Id(UUID categoryId);
}