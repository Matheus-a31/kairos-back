package com.kairos.project.dto;
import com.kairos.project.model.Project;

public record KanbanColumnRequest(
    String name,
    String color,
    Integer position
) {}


