package com.kairos.tag.dto;
import com.kairos.tag.model.Tag;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
    @NotBlank(message = "Nome é obrigatório")
    String name,
    
    @NotBlank(message = "Cor é obrigatória")
    String color
) {}


