package com.prumo.auth.dto;

import com.prumo.auth.model.Role;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role
) {}


