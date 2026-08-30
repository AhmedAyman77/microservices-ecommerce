package com.example.orderservice.share;

import lombok.Getter;

import java.util.List;

@Getter
public class GlobalResponse<T> {
    public final static String ERROR = "error";
    public final static String SUCCESS = "success";

    private final String status;
    private final T data;
    private final List<ErrorItems> errors;

    public GlobalResponse(T data) {
        this.data = data;
        this.errors = null;
        this.status = SUCCESS;
    }

    public GlobalResponse(List<ErrorItems> errors) {
        this.data = null;
        this.errors = errors;
        this.status = ERROR;
    }

    public record ErrorItems(String message) {}
}
