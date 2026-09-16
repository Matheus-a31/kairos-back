package com.kairos.auth.dto;
import com.kairos.auth.model.RefreshToken;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {}


