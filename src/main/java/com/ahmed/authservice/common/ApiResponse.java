package com.ahmed.authservice.common;

public record ApiResponse<T>(
        String message,
        T data
) {}