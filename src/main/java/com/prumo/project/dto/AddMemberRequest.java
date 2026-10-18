package com.prumo.project.dto;
import com.prumo.project.model.Project;
import com.prumo.auth.model.Role;


import com.prumo.project.model.ProjectRole;
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


