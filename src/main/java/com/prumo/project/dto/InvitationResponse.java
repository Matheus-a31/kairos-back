package com.prumo.project.dto;
import com.prumo.project.model.Project;
import com.prumo.auth.model.Role;


import com.prumo.project.model.ProjectRole;
import java.time.LocalDateTime;

public record InvitationResponse(
    Long id,
    Long projectId,
    String projectName,
    String email,
    ProjectRole role,
    LocalDateTime expiresAt,
    Boolean used
) {}


