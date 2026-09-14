package com.kairos.dto;

public record KanbanColumnRequest(
    String name,
    String color,
    Integer position
) {}
