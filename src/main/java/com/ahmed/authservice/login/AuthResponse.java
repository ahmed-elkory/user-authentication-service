package com.ahmed.authservice.login;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}