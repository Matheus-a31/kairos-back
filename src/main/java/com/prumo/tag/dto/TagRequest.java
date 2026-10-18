package com.prumo.tag.dto;
import com.prumo.tag.model.Tag;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
    @NotBlank(message = "Nome é obrigatório")
    String name,
    
    @NotBlank(message = "Cor é obrigatória")
    String color
) {}


