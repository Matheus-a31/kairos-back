package com.prumo.project.dto;
import com.prumo.project.model.Project;
import com.prumo.auth.model.Role;


import com.prumo.project.model.ProjectRole;
import java.time.LocalDateTime;

public record ProjectMemberResponse(
    Long id,
    Long userId,
    String userName,
    String userEmail,
    ProjectRole role,
    LocalDateTime joinedAt
) {}


