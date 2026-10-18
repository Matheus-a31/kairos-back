package com.kairos.task.dto;

import com.kairos.task.model.TaskStatus;

public record ChangeStatusRequest(
    TaskStatus status,
    Long columnId,
    String comment
) {
    public ChangeStatusRequest(TaskStatus status, String comment) {
        this(status, null, comment);
    }
}
