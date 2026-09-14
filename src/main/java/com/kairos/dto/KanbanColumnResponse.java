package com.kairos.dto;

public record KanbanColumnResponse(
    Long id,
    Long projectId,
    String name,
    String color,
    Integer position,
    Boolean isDefault
) {}
