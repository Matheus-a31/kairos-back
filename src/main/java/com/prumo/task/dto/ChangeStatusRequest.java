package com.prumo.task.dto;

import com.prumo.task.model.TaskStatus;

public record ChangeStatusRequest(
    TaskStatus status,
    Long columnId,
    String comment
) {
    public ChangeStatusRequest(TaskStatus status, String comment) {
        this(status, null, comment);
    }
}
