package com.kairos.dto;

import com.kairos.domain.ProjectStatus;
import java.time.LocalDate;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    ProjectStatus status,
    LocalDate startDate,
    LocalDate endDate
) {}
