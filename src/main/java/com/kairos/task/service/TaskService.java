package com.kairos.task.service;
import com.kairos.task.model.Task;
import com.kairos.task.model.TaskHistory;
import com.kairos.task.model.TaskStatus;
import com.kairos.task.repository.TaskHistoryRepository;
import com.kairos.task.repository.TaskRepository;
import com.kairos.task.model.TaskPriority;
import com.kairos.task.repository.TaskSpecification;
import com.kairos.project.repository.ProjectMemberRepository;
import com.kairos.project.repository.ProjectRepository;
import com.kairos.auth.repository.UserRepository;
import com.kairos.auth.model.User;
import com.kairos.project.model.Project;
import com.kairos.tag.model.Tag;
import com.kairos.tag.repository.TagRepository;


import com.kairos.task.dto.TaskRequest;
import com.kairos.task.dto.TaskResponse;
import com.kairos.core.exceptions.ResourceNotFoundException;
import com.kairos.task.mapper.TaskMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskHistoryRepository taskHistoryRepository, 
                       ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository,
                       UserRepository userRepository, TagRepository tagRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.taskMapper = taskMapper;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(Long projectId, String search, TaskStatus status, TaskPriority priority, Long assigneeId, Pageable pageable) {
        Specification<Task> spec = TaskSpecification.buildFilters(projectId, search, status, priority, assigneeId);
        return taskRepository.findAll(spec, pageable).map(taskMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long projectId, Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
        if (!task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Tarefa não pertence a este projeto");
        }
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        
        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        
        handleAssignee(projectId, request.assigneeId(), task);
        handleTags(projectId, request.tagIds(), task);
        
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, TaskRequest request) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
        if (!task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Tarefa não pertence a este projeto");
        }
        
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        
        handleAssignee(projectId, request.assigneeId(), task);
        handleTags(projectId, request.tagIds(), task);
        
        return taskMapper.toResponse(taskRepository.save(task));
    }
    
    @Transactional
    public TaskResponse changeStatus(Long projectId, Long taskId, TaskStatus newStatus, String comment) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
        if (!task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Tarefa não pertence a este projeto");
        }
        
        if (task.getStatus() == newStatus) {
            return taskMapper.toResponse(task);
        }
        
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        
        // Log history
        User currentUser = getCurrentUser();
        TaskHistory history = new TaskHistory(task, currentUser, oldStatus, newStatus, comment);
        taskHistoryRepository.save(history);
        
        return taskMapper.toResponse(taskRepository.save(task));
    }
    
    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
        if (!task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Tarefa não pertence a este projeto");
        }
        taskRepository.delete(task);
    }

    private void handleAssignee(Long projectId, Long assigneeId, Task task) {
        if (assigneeId != null) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)) {
                throw new IllegalArgumentException("Usuário não é membro deste projeto");
            }
            User assignee = userRepository.findById(assigneeId).orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado"));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }
    }

    private void handleTags(Long projectId, Set<Long> tagIds, Task task) {
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(tagIds);
            if (tags.stream().anyMatch(t -> !t.getProject().getId().equals(projectId))) {
                throw new IllegalArgumentException("Uma ou mais tags não pertencem a este projeto");
            }
            task.setTags(new HashSet<>(tags));
        } else {
            task.setTags(new HashSet<>());
        }
    }
}


