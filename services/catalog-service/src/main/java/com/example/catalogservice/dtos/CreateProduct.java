package com.example.catalogservice.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProduct(
    @NotNull(message = "Product name cannot be null")
    @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
    String name,

    @NotNull(message = "Product quantity cannot be null")
    Integer quantity,
    
    @NotNull(message = "Product price cannot be null")
    BigDecimal price,
    
    @NotNull(message = "Product category ID cannot be null")
    UUID categoryId
) {}
