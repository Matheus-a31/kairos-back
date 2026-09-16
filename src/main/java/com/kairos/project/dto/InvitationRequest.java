package com.kairos.project.dto;
import com.kairos.project.model.Project;
import com.kairos.auth.model.Role;


import com.kairos.project.model.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InvitationRequest(
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotNull(message = "O papel (role) é obrigatório")
    ProjectRole role
) {}


