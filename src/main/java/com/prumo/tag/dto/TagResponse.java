package com.prumo.tag.dto;
import com.prumo.tag.model.Tag;

public record TagResponse(
    Long id,
    String name,
    String color
) {}


