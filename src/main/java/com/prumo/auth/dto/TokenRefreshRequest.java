package com.prumo.auth.dto;
import com.prumo.auth.model.RefreshToken;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}


