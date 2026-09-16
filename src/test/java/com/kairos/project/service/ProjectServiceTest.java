package com.kairos.project.service;

import com.kairos.auth.model.Role;
import com.kairos.auth.model.User;
import com.kairos.auth.repository.UserRepository;
import com.kairos.project.dto.AddMemberRequest;
import com.kairos.project.dto.ProjectRequest;
import com.kairos.project.dto.ProjectResponse;
import com.kairos.project.mapper.ProjectMapper;
import com.kairos.project.model.Project;
import com.kairos.project.model.ProjectMember;
import com.kairos.project.model.ProjectRole;
import com.kairos.project.model.ProjectStatus;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User("Current User", "test@example.com", "pass", Role.MANAGER);
        ReflectionTestUtils_setId(currentUser, 1L);

        // Mock Security Context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void ReflectionTestUtils_setId(Object target, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(target, "id", id);
    }

    private void mockCurrentUser() {
        when(authentication.getName()).thenReturn(currentUser.getEmail());
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
    }

    @Test
    void createProject_ShouldSaveProjectAndAddCreatorAsManager() {
        mockCurrentUser();
        ProjectRequest request = new ProjectRequest("My Project", "Description", LocalDate.now(), LocalDate.now().plusDays(10), ProjectStatus.PLANNING);
        Project project = new Project();
        project.setName(request.name());
        ProjectResponse response = new ProjectResponse(1L, "My Project", "Description", null, null, ProjectStatus.PLANNING);

        when(projectMapper.toEntity(request)).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(response);

        ProjectResponse result = projectService.createProject(request);

        assertNotNull(result);
        assertEquals("My Project", result.name());
        verify(projectMemberRepository, times(1)).save(any(ProjectMember.class));
    }

    @Test
    void getProjectById_ShouldReturnProject_WhenUserIsMember() {
        mockCurrentUser();
        Project project = new Project();
        project.setName("Test");
        ProjectResponse response = new ProjectResponse(1L, "Test", "Desc", null, null, ProjectStatus.PLANNING);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, currentUser.getId())).thenReturn(true);
        when(projectMapper.toResponse(project)).thenReturn(response);

        ProjectResponse result = projectService.getProjectById(1L);

        assertNotNull(result);
        assertEquals("Test", result.name());
    }

    @Test
    void getProjectById_ShouldThrowException_WhenUserIsNotMember() {
        mockCurrentUser();
        Project project = new Project();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, currentUser.getId())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> projectService.getProjectById(1L));
        assertEquals("Você não tem acesso a este projeto", exception.getMessage());
    }

    @Test
    void addMember_ShouldAddUser_WhenUserIsNotYetMember() {
        Project project = new Project();
        ReflectionTestUtils_setId(project, 1L);
        User newMemberUser = new User("New", "new@example.com", "pass", Role.DEVELOPER);
        ReflectionTestUtils_setId(newMemberUser, 2L);

        AddMemberRequest request = new AddMemberRequest("new@example.com", ProjectRole.DEVELOPER);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(newMemberUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        com.kairos.project.dto.ProjectMemberResponse memberResponse = new com.kairos.project.dto.ProjectMemberResponse(1L, 2L, "New", "new@example.com", ProjectRole.DEVELOPER);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(new ProjectMember());
        when(projectMapper.toMemberResponse(any(ProjectMember.class))).thenReturn(memberResponse);

        var result = projectService.addMember(1L, request);

        assertNotNull(result);
        assertEquals("new@example.com", result.email());
    }
}
