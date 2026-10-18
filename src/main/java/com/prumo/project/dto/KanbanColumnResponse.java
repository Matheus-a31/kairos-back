package com.prumo.project.dto;
import com.prumo.project.model.Project;

public record KanbanColumnResponse(
    Long id,
    Long projectId,
    String name,
    String color,
    Integer position,
    Boolean isDefault
) {}


