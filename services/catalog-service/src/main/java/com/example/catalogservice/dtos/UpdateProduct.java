package com.example.catalogservice.dtos;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;


public record UpdateProduct(
    @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
    String name,
    Integer quantity,
    BigDecimal price,
    UUID category
) {}
