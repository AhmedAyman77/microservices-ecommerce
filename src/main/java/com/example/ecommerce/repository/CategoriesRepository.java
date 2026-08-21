package com.example.ecommerce.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.models.Categories;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, UUID> {
    boolean existsByName(String name);
}