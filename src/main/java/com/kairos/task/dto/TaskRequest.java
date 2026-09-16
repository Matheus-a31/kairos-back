package com.kairos.task.dto;
import com.kairos.task.model.Task;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;
import com.kairos.task.model.TaskPriority;

public record TaskRequest(
    @NotBlank(message = "O título é obrigatório")
    String title,
    String description,
    TaskPriority priority,
    LocalDate dueDate,
    Long assigneeId,
    Set<Long> tagIds
) {}


