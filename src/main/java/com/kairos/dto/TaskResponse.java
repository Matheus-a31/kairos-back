package com.kairos.dto;

import com.kairos.domain.TaskPriority;
import com.kairos.domain.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskPriority priority,
    TaskStatus status,
    LocalDate dueDate,
    Long assigneeId,
    String assigneeName,
    Set<TagResponse> tags,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
