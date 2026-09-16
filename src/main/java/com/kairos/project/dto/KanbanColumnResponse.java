package com.kairos.project.dto;
import com.kairos.project.model.Project;

public record KanbanColumnResponse(
    Long id,
    Long projectId,
    String name,
    String color,
    Integer position,
    Boolean isDefault
) {}


