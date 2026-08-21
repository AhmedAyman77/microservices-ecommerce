package com.example.ecommerce.dtos;

import com.example.ecommerce.enums.UserRole;

public record UserResponse(
        String username,
        String email,
        UserRole role
) {}
