package com.kairos.tag.dto;
import com.kairos.tag.model.Tag;

public record TagResponse(
    Long id,
    String name,
    String color
) {}


