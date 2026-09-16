package com.kairos.task.service;

import com.kairos.auth.model.Role;
import com.kairos.auth.model.User;
import com.kairos.auth.repository.UserRepository;
import com.kairos.project.model.Project;
import com.kairos.project.repository.ProjectMemberRepository;
import com.kairos.project.repository.ProjectRepository;
import com.kairos.tag.model.Tag;
import com.kairos.tag.repository.TagRepository;
import com.kairos.task.dto.TaskRequest;
import com.kairos.task.dto.TaskResponse;
import com.kairos.task.mapper.TaskMapper;
import com.kairos.task.model.Task;
import com.kairos.task.model.TaskHistory;
import com.kairos.task.model.TaskPriority;
import com.kairos.task.model.TaskStatus;
import com.kairos.task.repository.TaskHistoryRepository;
import com.kairos.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskHistoryRepository taskHistoryRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private User currentUser;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        currentUser = new User("Test User", "test@example.com", "pass", Role.MANAGER);
        org.springframework.test.util.ReflectionTestUtils.setField(currentUser, "id", 1L);

        project = new Project();
        project.setName("Test Project");
        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", 1L);

        task = new Task();
        task.setTitle("Test Task");
        task.setProject(project);
        task.setStatus(TaskStatus.TODO);
        org.springframework.test.util.ReflectionTestUtils.setField(task, "id", 1L);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockCurrentUser() {
        when(authentication.getName()).thenReturn(currentUser.getEmail());
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
    }

    @Test
    void getTasks_ShouldReturnPageOfTasks() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(task), pageRequest, 1);
        TaskResponse response = new TaskResponse(1L, "Test Task", null, null, TaskStatus.TODO, null, null, null, null, null, null);

        when(taskRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(page);
        when(taskMapper.toResponse(task)).thenReturn(response);

        Page<TaskResponse> result = taskService.getTasks(1L, null, null, null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Task", result.getContent().get(0).title());
    }

    @Test
    void createTask_ShouldSaveTask_WithAssigneeAndTags() {
        TaskRequest request = new TaskRequest("New Task", "Desc", TaskPriority.MEDIUM, LocalDate.now(), 2L, Set.of(1L));
        User assignee = new User("Assignee", "assignee@test.com", "pass", Role.MEMBER);
        org.springframework.test.util.ReflectionTestUtils.setField(assignee, "id", 2L);

        Tag tag = new Tag(project, "Urgent", "#F00");
        org.springframework.test.util.ReflectionTestUtils.setField(tag, "id", 1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskMapper.toEntity(request)).thenReturn(new Task());
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(tagRepository.findAllById(Set.of(1L))).thenReturn(List.of(tag));

        Task savedTask = new Task();
        savedTask.setTitle("New Task");
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        
        TaskResponse response = new TaskResponse(2L, "New Task", "Desc", TaskPriority.MEDIUM, TaskStatus.TODO, LocalDate.now(), 2L, "Assignee", null, null, null);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(response);

        TaskResponse result = taskService.createTask(1L, request);

        assertNotNull(result);
        assertEquals("New Task", result.title());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void changeStatus_ShouldSaveHistory_WhenStatusIsDifferent() {
        mockCurrentUser();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task updatedTask = new Task();
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        
        TaskResponse response = new TaskResponse(1L, "Test Task", null, null, TaskStatus.IN_PROGRESS, null, null, null, null, null, null);
        
        when(taskRepository.save(task)).thenReturn(updatedTask);
        when(taskMapper.toResponse(updatedTask)).thenReturn(response);

        TaskResponse result = taskService.changeStatus(1L, 1L, TaskStatus.IN_PROGRESS, "Start working");

        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, result.status());
        verify(taskHistoryRepository, times(1)).save(any(TaskHistory.class));
    }

    @Test
    void changeStatus_ShouldThrowException_WhenTaskNotFromProject() {
        Project otherProject = new Project();
        org.springframework.test.util.ReflectionTestUtils.setField(otherProject, "id", 2L);
        task.setProject(otherProject);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> taskService.changeStatus(1L, 1L, TaskStatus.IN_PROGRESS, ""));
        assertEquals("Tarefa não pertence a este projeto", exception.getMessage());
    }
}
