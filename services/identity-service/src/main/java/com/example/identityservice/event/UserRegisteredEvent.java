package com.example.identityservice.event;

public record UserRegisteredEvent(
        String email,
        String token
) {}