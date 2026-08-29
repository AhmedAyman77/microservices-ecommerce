package com.example.catalogservice.dtos;

import jakarta.validation.constraints.Size;

public record UpdateCategory(
        @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
        String name
) {}
