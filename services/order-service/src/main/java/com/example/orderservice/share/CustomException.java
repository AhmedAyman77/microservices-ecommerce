package com.example.orderservice.share;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException {
    private int statusCode;
    private String message;
    
    public static CustomException badRequest(String message) {
        return new CustomException(400, message);
    }
    
    public static CustomException badCredentials() {
        return new CustomException(401, "Bad credentials");
    }

    public static CustomException resourceNotFound(String message) {
        return new CustomException(404, message);
    }

    public static CustomException conflict(String message) {
        return new CustomException(409, message);
    }

    public static CustomException internalServerError(String message) {
        return new CustomException(500, message);
    }
}
