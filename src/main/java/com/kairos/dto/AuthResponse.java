package com.kairos.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {}
