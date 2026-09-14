package com.kairos.dto;

import com.kairos.domain.ProjectRole;
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
