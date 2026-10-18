package com.kairos.task.service;

import com.kairos.task.model.Task;
import com.kairos.task.model.TaskHistory;
import com.kairos.task.model.TaskStatus;
import com.kairos.task.repository.TaskHistoryRepository;
import com.kairos.task.repository.TaskRepository;
import com.kairos.task.model.TaskPriority;
import com.kairos.task.repository.TaskSpecification;
import com.kairos.project.model.KanbanColumn;
import com.kairos.project.model.Project;
import com.kairos.project.model.ProjectMember;
import com.kairos.project.model.ProjectRole;
import com.kairos.project.repository.KanbanColumnRepository;
import com.kairos.project.repository.ProjectMemberRepository;
import com.kairos.project.repository.ProjectRepository;
import com.kairos.auth.repository.UserRepository;
import com.kairos.auth.model.User;
import com.kairos.tag.model.Tag;
import com.kairos.tag.repository.TagRepository;
import com.kairos.notification.service.NotificationService;
import com.kairos.notification.model.NotificationType;
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
    private final KanbanColumnRepository kanbanColumnRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository, TaskHistoryRepository taskHistoryRepository, 
                       ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository,
                       KanbanColumnRepository kanbanColumnRepository,
                       UserRepository userRepository, TagRepository tagRepository, TaskMapper taskMapper,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.kanbanColumnRepository = kanbanColumnRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.taskMapper = taskMapper;
        this.notificationService = notificationService;
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

        if (kanbanColumnRepository != null) {
            List<KanbanColumn> columns = kanbanColumnRepository.findByProjectIdOrderByPosition(projectId);
            if (!columns.isEmpty()) {
                task.setKanbanColumn(columns.get(0));
            }
        }
        
        task = taskRepository.save(task);
        
        if (task.getAssignee() != null) {
            notificationService.createNotification(task.getAssignee(), project, "Você foi atribuído à tarefa: " + task.getTitle(), NotificationType.TASK_ASSIGNED);
        }
        
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
        
        User oldAssignee = task.getAssignee();
        
        handleAssignee(projectId, request.assigneeId(), task);
        handleTags(projectId, request.tagIds(), task);
        
        task = taskRepository.save(task);
        
        if (task.getAssignee() != null && (oldAssignee == null || !oldAssignee.getId().equals(task.getAssignee().getId()))) {
            notificationService.createNotification(task.getAssignee(), task.getProject(), "Você foi atribuído à tarefa: " + task.getTitle(), NotificationType.TASK_ASSIGNED);
        }
        
        return taskMapper.toResponse(task);
    }
    
    @Transactional
    public TaskResponse changeStatus(Long projectId, Long taskId, TaskStatus newStatus, String comment) {
        return changeStatus(projectId, taskId, newStatus, null, comment);
    }

    @Transactional
    public TaskResponse changeStatus(Long projectId, Long taskId, TaskStatus newStatus, Long columnId, String comment) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
        if (!task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Tarefa não pertence a este projeto");
        }

        KanbanColumn column = null;
        if (columnId != null && kanbanColumnRepository != null) {
            column = kanbanColumnRepository.findById(columnId)
                    .orElseThrow(() -> new ResourceNotFoundException("Coluna não encontrada"));
            if (!column.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Coluna não pertence a este projeto");
            }
            if (newStatus == null) {
                newStatus = deduceStatusFromColumn(column);
            }
        }
        
        TaskStatus oldStatus = task.getStatus();
        KanbanColumn oldColumn = task.getKanbanColumn();

        boolean statusChanged = (newStatus != null && task.getStatus() != newStatus);
        boolean columnChanged = (column != null && (oldColumn == null || !oldColumn.getId().equals(column.getId())));

        if (!statusChanged && !columnChanged) {
            return taskMapper.toResponse(task);
        }
        
        if (newStatus != null) {
            task.setStatus(newStatus);
        }
        if (column != null) {
            task.setKanbanColumn(column);
        }
        
        // Log history
        User currentUser = getCurrentUser();
        TaskHistory history = new TaskHistory(task, currentUser, oldStatus, task.getStatus(), comment);
        taskHistoryRepository.save(history);
        
        task = taskRepository.save(task);

        // Notificações
        String destinationName = (column != null) ? column.getName() : task.getStatus().name();
        notifyProjectManagersAndAssignee(projectId, task, currentUser, destinationName);
        
        return taskMapper.toResponse(task);
    }

    private TaskStatus deduceStatusFromColumn(KanbanColumn column) {
        String colName = column.getName().toLowerCase();
        if (colName.contains("conclu") || colName.contains("done") || colName.contains("final")) {
            return TaskStatus.DONE;
        } else if (colName.contains("fazer") || colName.contains("todo") || colName.contains("backlog") || colName.contains("pendente") || column.getPosition() == 0) {
            return TaskStatus.TODO;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }

    private void notifyProjectManagersAndAssignee(Long projectId, Task task, User currentUser, String destinationName) {
        // Notifica gerentes do projeto (exceto o próprio usuário que alterou)
        List<ProjectMember> managers = projectMemberRepository.findByProjectIdAndRole(projectId, ProjectRole.MANAGER);
        for (ProjectMember managerMember : managers) {
            if (!managerMember.getUser().getId().equals(currentUser.getId())) {
                notificationService.createNotification(
                    managerMember.getUser(),
                    task.getProject(),
                    "A tarefa '" + task.getTitle() + "' foi movida para " + destinationName + " por " + currentUser.getName(),
                    NotificationType.TASK_UPDATED
                );
            }
        }

        // Notifica o responsável da tarefa (se existir e não for quem fez a alteração)
        if (task.getAssignee() != null && !task.getAssignee().getId().equals(currentUser.getId())) {
            notificationService.createNotification(
                task.getAssignee(),
                task.getProject(),
                "A tarefa '" + task.getTitle() + "' atribuída a você foi movida para " + destinationName + " por " + currentUser.getName(),
                NotificationType.TASK_UPDATED
            );
        }
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
