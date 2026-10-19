package com.prumo.project.service;

import com.prumo.task.model.Task;
import java.util.Optional;
import com.prumo.project.model.KanbanColumn;
import com.prumo.project.model.ProjectMember;
import com.prumo.project.dto.ProjectMemberResponse;
import com.prumo.project.model.Project;
import com.prumo.project.model.ProjectStatus;
import com.prumo.project.dto.AddMemberRequest;
import com.prumo.project.model.ProjectRole;
import com.prumo.auth.model.Role;
import com.prumo.task.repository.TaskRepository;
import com.prumo.task.service.TaskService;
import com.prumo.auth.model.User;
import com.prumo.notification.model.NotificationType;
import com.prumo.notification.service.NotificationService;
import com.prumo.project.dto.ProjectRequest;
import com.prumo.project.dto.ProjectResponse;
import com.prumo.core.exceptions.ResourceNotFoundException;
import com.prumo.project.mapper.ProjectMapper;
import com.prumo.project.repository.KanbanColumnRepository;
import com.prumo.project.repository.ProjectMemberRepository;
import com.prumo.project.repository.ProjectRepository;
import com.prumo.auth.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final KanbanColumnRepository kanbanColumnRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          KanbanColumnRepository kanbanColumnRepository,
                          UserRepository userRepository,
                          ProjectMapper projectMapper,
                          NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.kanbanColumnRepository = kanbanColumnRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
        this.notificationService = notificationService;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User currentUser = getCurrentUser();
        
        Project project = projectMapper.toEntity(request);
        project = projectRepository.save(project);

        // O criador se torna MANAGER automaticamente
        ProjectMember member = new ProjectMember(project, currentUser, ProjectRole.MANAGER);
        projectMemberRepository.save(member);

        // Cria colunas Kanban padrão para o projeto
        if (kanbanColumnRepository != null) {
            kanbanColumnRepository.save(new KanbanColumn(project, "A Fazer", "#64748B", 0, true));
            kanbanColumnRepository.save(new KanbanColumn(project, "Em Andamento", "#3B82F6", 1, true));
            kanbanColumnRepository.save(new KanbanColumn(project, "Concluído", "#22C55E", 2, true));
        }
        
        return projectMapper.toResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(String nameFilter, Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Project> projects;
        
        if (nameFilter != null && !nameFilter.trim().isEmpty()) {
            projects = projectRepository.findProjectsByUserIdAndName(currentUser.getId(), nameFilter, pageable);
        } else {
            projects = projectRepository.findProjectsByUserId(currentUser.getId(), pageable);
        }
        
        return projects.map(projectMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
                
        // Check if user is member
        if (!projectMemberRepository.existsByProjectIdAndUserId(id, currentUser.getId())) {
            throw new IllegalArgumentException("Você não tem acesso a este projeto");
        }
        
        return projectMapper.toResponse(project);
    }
    
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
                
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Você não tem acesso a este projeto"));
                
        if (member.getRole() != ProjectRole.MANAGER) {
            throw new IllegalArgumentException("Apenas o gerente do projeto pode atualizá-lo");
        }
        
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        
        return projectMapper.toResponse(projectRepository.save(project));
    }
    
    @Transactional
    public ProjectResponse updateProjectBackground(Long id, String backgroundColor) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
                
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Você não tem acesso a este projeto"));
                
        if (member.getRole() != ProjectRole.MANAGER) {
            throw new IllegalArgumentException("Apenas o gerente do projeto pode atualizá-lo");
        }
        
        project.setBackgroundColor(backgroundColor);
        return projectMapper.toResponse(projectRepository.save(project));
    }
    
    @Transactional
    public void deleteProject(Long id) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
                
        boolean isSystemManager = currentUser.getRole() == com.prumo.auth.model.Role.ADMIN || currentUser.getRole() == com.prumo.auth.model.Role.MANAGER;
        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(id, currentUser.getId());
        boolean isProjectManager = memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.MANAGER;

        if (!isSystemManager && !isProjectManager) {
            throw new IllegalArgumentException("Apenas o gerente do projeto ou administrador pode excluí-lo");
        }
        
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new IllegalArgumentException("Projetos concluídos não podem ser excluídos");
        }
        
        projectRepository.delete(project);
    }
    
    // --- Membros --- //

    @Transactional(readOnly = true)
    public Page<com.prumo.project.dto.ProjectMemberResponse> getProjectMembers(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        
        return new org.springframework.data.domain.PageImpl<>(
            project.getMembers().stream().map(projectMapper::toMemberResponse).toList(), 
            pageable, 
            project.getMembers().size()
        );
    }

    @Transactional
    public com.prumo.project.dto.ProjectMemberResponse addMember(Long projectId, com.prumo.project.dto.AddMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
                
        User targetUser = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com este e-mail"));
                
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, targetUser.getId())) {
            throw new IllegalArgumentException("Usuário já é membro deste projeto");
        }
        
        ProjectMember newMember = new ProjectMember(project, targetUser, request.role());
        newMember = projectMemberRepository.save(newMember);
        
        notificationService.createNotification(targetUser, project, "Você foi adicionado ao projeto " + project.getName() + " como " + request.role().name(), NotificationType.PROJECT_INVITE);
        
        return projectMapper.toMemberResponse(newMember);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        ProjectMember memberToRemove = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado neste projeto"));
                
        boolean hasCriticalTasks = checkMockedCriticalTasks(projectId, userId);
        if (hasCriticalTasks) {
            throw new IllegalArgumentException("Usuário não pode ser removido pois possui tarefas críticas atribuídas.");
        }
        
        // Impede que o único gerente seja removido se não houver outro
        if (memberToRemove.getRole() == ProjectRole.MANAGER) {
            long managerCount = memberToRemove.getProject().getMembers().stream()
                .filter(m -> m.getRole() == ProjectRole.MANAGER).count();
            if (managerCount <= 1) {
                throw new IllegalArgumentException("O projeto precisa ter pelo menos um gerente ativo.");
            }
        }
        
        projectMemberRepository.delete(memberToRemove);
    }
    
    private boolean checkMockedCriticalTasks(Long projectId, Long userId) {
        return false;
    }
}
