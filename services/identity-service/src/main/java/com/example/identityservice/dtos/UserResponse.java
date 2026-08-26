package com.example.identityservice.dtos;

import com.example.identityservice.enums.UserRole;

public record UserResponse(
        String username,
        String email,
        UserRole role
) {}
