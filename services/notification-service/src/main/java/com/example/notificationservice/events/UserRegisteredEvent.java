package com.example.notificationservice.events;

public record UserRegisteredEvent(
        String email,
        String token
) {}