package com.prumo.project.dto;
import com.prumo.project.model.Project;

import com.prumo.project.model.ProjectStatus;
import java.time.LocalDate;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    ProjectStatus status,
    LocalDate startDate,
    LocalDate endDate,
    String backgroundColor
) {}


