package com.kairos.auth.dto;
import com.kairos.auth.model.RefreshToken;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}


