package com.kairos.project.dto;
import com.kairos.project.model.Project;
import com.kairos.auth.model.Role;


import com.kairos.project.model.ProjectRole;
import java.time.LocalDateTime;

public record ProjectMemberResponse(
    Long id,
    Long userId,
    String userName,
    String userEmail,
    ProjectRole role,
    LocalDateTime joinedAt
) {}


