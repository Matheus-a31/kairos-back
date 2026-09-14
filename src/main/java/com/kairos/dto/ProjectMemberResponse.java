package com.kairos.dto;

import com.kairos.domain.ProjectRole;
import java.time.LocalDateTime;

public record ProjectMemberResponse(
    Long id,
    Long userId,
    String userName,
    String userEmail,
    ProjectRole role,
    LocalDateTime joinedAt
) {}
