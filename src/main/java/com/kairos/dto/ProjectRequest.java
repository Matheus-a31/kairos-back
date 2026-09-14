package com.kairos.dto;

import com.kairos.domain.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ProjectRequest(
    @NotBlank(message = "O nome do projeto é obrigatório")
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    ProjectStatus status
) {}
