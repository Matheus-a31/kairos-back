package com.prumo.auth.dto;
import com.prumo.auth.model.RefreshToken;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {}


