package com.kairos.project.service;

import com.kairos.auth.model.Role;
import com.kairos.auth.model.User;
import com.kairos.auth.repository.UserRepository;
import com.kairos.core.email.EmailService;
import com.kairos.project.dto.InvitationRequest;
import com.kairos.project.model.Project;
import com.kairos.project.model.ProjectInvitation;
import com.kairos.project.model.ProjectMember;
import com.kairos.project.model.ProjectRole;
import com.kairos.project.repository.ProjectInvitationRepository;
import com.kairos.project.repository.ProjectMemberRepository;
import com.kairos.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectInvitationServiceTest {

    @Mock
    private ProjectInvitationRepository invitationRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectInvitationService invitationService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User("Current User", "test@example.com", "pass", Role.MANAGER);
        org.springframework.test.util.ReflectionTestUtils.setField(currentUser, "id", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(invitationService, "frontendUrl", "http://localhost:4200");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockCurrentUser() {
        when(authentication.getName()).thenReturn(currentUser.getEmail());
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
    }

    @Test
    void createInvitation_ShouldSaveAndSendEmail_WhenManagerRequests() {
        mockCurrentUser();
        Project project = new Project();
        project.setName("Kairos Proj");
        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", 1L);

        ProjectMember managerMember = new ProjectMember(project, currentUser, ProjectRole.MANAGER);

        when(memberRepository.findByProjectIdAndUserId(1L, 1L)).thenReturn(Optional.of(managerMember));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(invitationRepository.existsByProjectIdAndEmailAndUsedFalse(1L, "new@example.com")).thenReturn(false);

        InvitationRequest request = new InvitationRequest("new@example.com", ProjectRole.DEVELOPER);

        invitationService.createInvitation(1L, request);

        verify(invitationRepository, times(1)).save(any(ProjectInvitation.class));
        verify(emailService, times(1)).sendInvitationEmail(eq("new@example.com"), eq("Kairos Proj"), anyString());
    }

    @Test
    void createInvitation_ShouldThrowException_WhenNotManager() {
        mockCurrentUser();
        ProjectMember viewerMember = new ProjectMember(new Project(), currentUser, ProjectRole.VIEWER);
        when(memberRepository.findByProjectIdAndUserId(1L, 1L)).thenReturn(Optional.of(viewerMember));

        InvitationRequest request = new InvitationRequest("new@example.com", ProjectRole.DEVELOPER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> invitationService.createInvitation(1L, request));
        assertEquals("Apenas o gerente pode enviar convites", exception.getMessage());
    }

    @Test
    void acceptInvitation_ShouldAddMemberAndMarkAsUsed_WhenValid() {
        mockCurrentUser();
        Project project = new Project();
        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", 1L);
        
        ProjectInvitation invitation = new ProjectInvitation(project, currentUser.getEmail(), "token123", ProjectRole.DEVELOPER);

        when(invitationRepository.findByToken("token123")).thenReturn(Optional.of(invitation));
        when(memberRepository.findByProjectIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        invitationService.acceptInvitation("token123");

        verify(memberRepository, times(1)).save(any(ProjectMember.class));
        assertTrue(invitation.getUsed());
        verify(invitationRepository, times(1)).save(invitation);
    }

    @Test
    void acceptInvitation_ShouldThrowException_WhenExpired() {
        mockCurrentUser();
        ProjectInvitation invitation = new ProjectInvitation(new Project(), currentUser.getEmail(), "token123", ProjectRole.DEVELOPER);
        org.springframework.test.util.ReflectionTestUtils.setField(invitation, "expiresAt", LocalDateTime.now().minusDays(1)); // Vencido

        when(invitationRepository.findByToken("token123")).thenReturn(Optional.of(invitation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> invitationService.acceptInvitation("token123"));
        assertEquals("Este convite expirou", exception.getMessage());
        verify(memberRepository, never()).save(any(ProjectMember.class));
    }
}
