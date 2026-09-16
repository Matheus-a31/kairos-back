package com.kairos.project.dto;
import com.kairos.project.model.Project;

import com.kairos.project.model.ProjectStatus;
import java.time.LocalDate;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    ProjectStatus status,
    LocalDate startDate,
    LocalDate endDate
) {}


