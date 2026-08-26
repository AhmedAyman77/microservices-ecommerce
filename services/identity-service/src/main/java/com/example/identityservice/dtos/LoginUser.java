package com.example.identityservice.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginUser(
    @NotNull(message = "Username is required")
    @Size(min = 3, message = "Username must be at least 3 characters long")
    String username,

    @NotNull(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password
) {}
