package com.prumo.task.dto;
import com.prumo.task.model.Task;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;
import com.prumo.task.model.TaskPriority;

import com.prumo.task.model.TaskStatus;

public record TaskRequest(
    @NotBlank(message = "O título é obrigatório")
    String title,
    String description,
    TaskPriority priority,
    TaskStatus status,
    LocalDate dueDate,
    Long assigneeId,
    Set<Long> tagIds
) {
    public TaskRequest(String title, String description, TaskPriority priority, LocalDate dueDate, Long assigneeId, Set<Long> tagIds) {
        this(title, description, priority, null, dueDate, assigneeId, tagIds);
    }
}


