package com.prumo.project.dto;
import com.prumo.project.model.Project;

public record KanbanColumnRequest(
    String name,
    String color,
    Integer position
) {}


