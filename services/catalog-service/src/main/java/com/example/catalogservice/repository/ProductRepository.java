package com.example.catalogservice.repository;

import com.example.catalogservice.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Products, UUID> {
    boolean existsByCategory_Id(UUID categoryId);
}