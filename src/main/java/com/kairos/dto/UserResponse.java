package com.kairos.dto;

import com.kairos.domain.Role;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role
) {}
