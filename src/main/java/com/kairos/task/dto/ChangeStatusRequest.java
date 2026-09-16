package com.kairos.task.dto;
import com.kairos.task.model.Task;

import com.kairos.task.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
    @NotNull(message = "O status é obrigatório")
    TaskStatus status,
    
    String comment
) {}


