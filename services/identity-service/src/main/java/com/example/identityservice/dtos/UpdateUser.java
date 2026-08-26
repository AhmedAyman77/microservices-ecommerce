package com.example.identityservice.dtos;

import jakarta.validation.constraints.Size;

public record UpdateUser(
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,

    @Size(max = 100, message = "Email must be at most 100 characters")
    String email
) {}
