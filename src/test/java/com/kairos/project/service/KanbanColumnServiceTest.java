package com.kairos.project.service;

import com.kairos.auth.model.Role;
import com.kairos.auth.model.User;
import com.kairos.auth.repository.UserRepository;
import com.kairos.project.dto.KanbanColumnRequest;
import com.kairos.project.dto.KanbanColumnResponse;
import com.kairos.project.model.KanbanColumn;
import com.kairos.project.model.Project;
import com.kairos.project.model.ProjectMember;
import com.kairos.project.model.ProjectRole;
import com.kairos.project.repository.KanbanColumnRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KanbanColumnServiceTest {

    @Mock
    private KanbanColumnRepository columnRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private KanbanColumnService columnService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User("Current User", "test@example.com", "pass", Role.MANAGER);
        org.springframework.test.util.ReflectionTestUtils.setField(currentUser, "id", 1L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockCurrentUser() {
        when(authentication.getName()).thenReturn(currentUser.getEmail());
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
    }

    @Test
    void getColumns_ShouldReturnList_WhenProjectExists() {
        KanbanColumn column = new KanbanColumn();
        Project project = new Project();
        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", 1L);
        column.setProject(project);
        column.setName("To Do");
        org.springframework.test.util.ReflectionTestUtils.setField(column, "id", 1L);

        when(columnRepository.findByProjectIdOrderByPosition(1L)).thenReturn(List.of(column));

        List<KanbanColumnResponse> result = columnService.getColumns(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("To Do", result.get(0).name());
    }

    @Test
    void createColumn_ShouldSave_WhenManagerRequests() {
        mockCurrentUser();
        Project project = new Project();
        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", 1L);

        ProjectMember managerMember = new ProjectMember(project, currentUser, ProjectRole.MANAGER);

        when(memberRepository.findByProjectIdAndUserId(1L, 1L)).thenReturn(Optional.of(managerMember));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(columnRepository.findByProjectIdOrderByPosition(1L)).thenReturn(List.of());

        KanbanColumn savedColumn = new KanbanColumn(project, "Doing", "#FFFFFF", 0, false);
        org.springframework.test.util.ReflectionTestUtils.setField(savedColumn, "id", 1L);
        
        when(columnRepository.save(any(KanbanColumn.class))).thenReturn(savedColumn);

        KanbanColumnRequest request = new KanbanColumnRequest("Doing", "#FFFFFF", null);
        KanbanColumnResponse result = columnService.createColumn(1L, request);

        assertNotNull(result);
        assertEquals("Doing", result.name());
        verify(columnRepository, times(1)).save(any(KanbanColumn.class));
    }

    @Test
    void deleteColumn_ShouldDelete_WhenNotDefaultAndRequestedByManager() {
        mockCurrentUser();
        Project project = new Project();
        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", 1L);
        ProjectMember managerMember = new ProjectMember(project, currentUser, ProjectRole.MANAGER);

        KanbanColumn column = new KanbanColumn(project, "Review", "#FFFFFF", 0, false);
        org.springframework.test.util.ReflectionTestUtils.setField(column, "id", 2L);

        when(memberRepository.findByProjectIdAndUserId(1L, 1L)).thenReturn(Optional.of(managerMember));
        when(columnRepository.findById(2L)).thenReturn(Optional.of(column));

        assertDoesNotThrow(() -> columnService.deleteColumn(1L, 2L));
        verify(columnRepository, times(1)).delete(column);
    }

    @Test
    void deleteColumn_ShouldThrowException_WhenColumnIsDefault() {
        mockCurrentUser();
        Project project = new Project();
        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", 1L);
        ProjectMember managerMember = new ProjectMember(project, currentUser, ProjectRole.MANAGER);

        KanbanColumn column = new KanbanColumn(project, "To Do", "#FFFFFF", 0, true); // true = default
        org.springframework.test.util.ReflectionTestUtils.setField(column, "id", 2L);

        when(memberRepository.findByProjectIdAndUserId(1L, 1L)).thenReturn(Optional.of(managerMember));
        when(columnRepository.findById(2L)).thenReturn(Optional.of(column));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> columnService.deleteColumn(1L, 2L));
        assertEquals("Colunas padrão não podem ser excluídas", exception.getMessage());
        verify(columnRepository, never()).delete(column);
    }
}
