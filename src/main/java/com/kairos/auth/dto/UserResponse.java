package com.kairos.auth.dto;

import com.kairos.auth.model.Role;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role
) {}


