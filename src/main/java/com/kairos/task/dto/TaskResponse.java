package com.kairos.task.dto;
import com.kairos.task.model.Task;
import com.kairos.tag.model.Tag;
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
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}


