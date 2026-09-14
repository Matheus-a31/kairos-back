package com.kairos.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;
import com.kairos.domain.TaskPriority;

public record TaskRequest(
    @NotBlank(message = "O título é obrigatório")
    String title,
    String description,
    TaskPriority priority,
    LocalDate dueDate,
    Long assigneeId,
    Set<Long> tagIds
) {}
