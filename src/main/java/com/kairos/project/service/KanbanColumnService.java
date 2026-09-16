package com.kairos.project.service;

import com.kairos.project.model.KanbanColumn;
import com.kairos.project.model.Project;
import com.kairos.project.model.ProjectMember;
import com.kairos.project.model.ProjectRole;
import com.kairos.auth.model.User;
import com.kairos.project.dto.KanbanColumnRequest;
import com.kairos.project.dto.KanbanColumnResponse;
import com.kairos.core.exceptions.ResourceNotFoundException;
import com.kairos.project.repository.KanbanColumnRepository;
import com.kairos.project.repository.ProjectMemberRepository;
import com.kairos.project.repository.ProjectRepository;
import com.kairos.auth.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KanbanColumnService {

    private final KanbanColumnRepository columnRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    public KanbanColumnService(KanbanColumnRepository columnRepository,
                               ProjectRepository projectRepository,
                               ProjectMemberRepository memberRepository,
                               UserRepository userRepository) {
        this.columnRepository = columnRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
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
            throw new IllegalArgumentException("Apenas o gerente pode gerenciar as colunas do Kanban");
        }
    }

    @Transactional(readOnly = true)
    public List<KanbanColumnResponse> getColumns(Long projectId) {
        return columnRepository.findByProjectIdOrderByPosition(projectId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public KanbanColumnResponse createColumn(Long projectId, KanbanColumnRequest request) {
        assertManager(projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        // Set position at end if not provided
        int position = request.position() != null ? request.position() :
                columnRepository.findByProjectIdOrderByPosition(projectId).size();

        KanbanColumn column = new KanbanColumn(
                project,
                request.name(),
                request.color() != null ? request.color() : "#94A3B8",
                position,
                false
        );
        return toResponse(columnRepository.save(column));
    }

    @Transactional
    public KanbanColumnResponse updateColumn(Long projectId, Long columnId, KanbanColumnRequest request) {
        assertManager(projectId);
        KanbanColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna não encontrada"));

        if (!column.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Coluna não pertence a este projeto");
        }

        if (request.name() != null) column.setName(request.name());
        if (request.color() != null) column.setColor(request.color());
        if (request.position() != null) column.setPosition(request.position());

        return toResponse(columnRepository.save(column));
    }

    @Transactional
    public void deleteColumn(Long projectId, Long columnId) {
        assertManager(projectId);
        KanbanColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna não encontrada"));

        if (!column.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Coluna não pertence a este projeto");
        }

        if (Boolean.TRUE.equals(column.getIsDefault())) {
            throw new IllegalArgumentException("Colunas padrão não podem ser excluídas");
        }

        columnRepository.delete(column);
    }

    private KanbanColumnResponse toResponse(KanbanColumn col) {
        return new KanbanColumnResponse(
                col.getId(),
                col.getProject().getId(),
                col.getName(),
                col.getColor(),
                col.getPosition(),
                col.getIsDefault()
        );
    }
}


