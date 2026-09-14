package com.kairos.service;

import com.kairos.domain.*;
import com.kairos.dto.InvitationRequest;
import com.kairos.dto.InvitationResponse;
import com.kairos.exceptions.ResourceNotFoundException;
import com.kairos.repository.ProjectInvitationRepository;
import com.kairos.repository.ProjectMemberRepository;
import com.kairos.repository.ProjectRepository;
import com.kairos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProjectInvitationService {

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${kairos.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public ProjectInvitationService(ProjectInvitationRepository invitationRepository,
                                    ProjectRepository projectRepository,
                                    ProjectMemberRepository memberRepository,
                                    UserRepository userRepository,
                                    EmailService emailService) {
        this.invitationRepository = invitationRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private void assertManager(Long projectId) {
        User user = getCurrentUser();
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Você não é membro deste projeto"));
        if (member.getRole() != ProjectRole.MANAGER) {
            throw new IllegalArgumentException("Apenas o gerente pode enviar convites");
        }
    }

    @Transactional
    public void createInvitation(Long projectId, InvitationRequest request) {
        assertManager(projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        // Check if user is already a member
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            if (memberRepository.findByProjectIdAndUserId(projectId, user.getId()).isPresent()) {
                throw new IllegalArgumentException("O usuário já é membro deste projeto");
            }
        });

        // Check if there is an active pending invite for this email
        if (invitationRepository.existsByProjectIdAndEmailAndUsedFalse(projectId, request.email())) {
            throw new IllegalArgumentException("Já existe um convite pendente para este email neste projeto");
        }

        String token = UUID.randomUUID().toString();
        ProjectInvitation invitation = new ProjectInvitation(project, request.email(), token, request.role());
        invitationRepository.save(invitation);

        String inviteLink = frontendUrl + "/invite?token=" + token;
        emailService.sendInvitationEmail(request.email(), project.getName(), inviteLink);
    }

    @Transactional(readOnly = true)
    public InvitationResponse getInvitationInfo(String token) {
        ProjectInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Convite inválido ou não encontrado"));

        if (invitation.getUsed()) {
            throw new IllegalArgumentException("Este convite já foi utilizado");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Este convite expirou");
        }

        return new InvitationResponse(
                invitation.getId(),
                invitation.getProject().getId(),
                invitation.getProject().getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getExpiresAt(),
                invitation.getUsed()
        );
    }

    @Transactional
    public void acceptInvitation(String token) {
        User user = getCurrentUser();

        ProjectInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Convite inválido ou não encontrado"));

        if (invitation.getUsed()) {
            throw new IllegalArgumentException("Este convite já foi utilizado");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Este convite expirou");
        }

        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new IllegalArgumentException("Este convite foi enviado para outro email");
        }

        Project project = invitation.getProject();
        
        // Check if already member
        if (memberRepository.findByProjectIdAndUserId(project.getId(), user.getId()).isEmpty()) {
            ProjectMember newMember = new ProjectMember();
            newMember.setProject(project);
            newMember.setUser(user);
            newMember.setRole(invitation.getRole());
            memberRepository.save(newMember);
        }

        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }
}
