package com.kairos.task.dto;

import com.kairos.tag.dto.TagResponse;
import com.kairos.task.model.TaskPriority;
import com.kairos.task.model.TaskStatus;
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
    Long kanbanColumnId,
    String kanbanColumnName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public TaskResponse(
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
    ) {
        this(id, title, description, priority, status, dueDate, assigneeId, assigneeName, tags, null, null, createdAt, updatedAt);
    }
}
