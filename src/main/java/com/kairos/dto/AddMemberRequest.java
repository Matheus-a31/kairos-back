package com.kairos.dto;

import com.kairos.domain.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,

    @NotNull(message = "O perfil (role) é obrigatório")
    ProjectRole role
) {}
