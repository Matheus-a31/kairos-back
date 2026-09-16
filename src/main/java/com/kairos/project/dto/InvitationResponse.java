package com.kairos.project.dto;
import com.kairos.project.model.Project;
import com.kairos.auth.model.Role;


import com.kairos.project.model.ProjectRole;
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


