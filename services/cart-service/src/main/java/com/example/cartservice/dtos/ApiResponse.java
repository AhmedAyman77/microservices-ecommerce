package com.example.cartservice.dtos;

import java.util.List;

public record ApiResponse<T>(String status, T data, List<ErrorItem> errors) {
    public record ErrorItem(String message) {}
}