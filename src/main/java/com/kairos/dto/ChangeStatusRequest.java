package com.kairos.dto;

import com.kairos.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
    @NotNull(message = "O status é obrigatório")
    TaskStatus status,
    
    String comment
) {}
