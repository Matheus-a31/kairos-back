package com.prumo.project.controller;
import com.prumo.project.dto.ProjectMemberResponse;
import com.prumo.project.security.ProjectSecurity;
import com.prumo.project.model.Project;
import com.prumo.project.dto.AddMemberRequest;

import com.prumo.project.dto.ProjectRequest;
import com.prumo.project.dto.ProjectResponse;
import com.prumo.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getProjects(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjects(name, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PutMapping("/{id}/background")
    @PreAuthorize("@projectSecurity.isManager(authentication, #id)")
    public ResponseEntity<ProjectResponse> updateProjectBackground(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> request) {
        return ResponseEntity.ok(projectService.updateProjectBackground(id, request.get("backgroundColor")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectSecurity.isManager(authentication, #id)")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints de Membros --- //

    @GetMapping("/{id}/members")
    @PreAuthorize("@projectSecurity.isMemberOrManager(authentication, #id)")
    public ResponseEntity<Page<com.prumo.project.dto.ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectMembers(id, pageable));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("@projectSecurity.isManager(authentication, #id)")
    public ResponseEntity<com.prumo.project.dto.ProjectMemberResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody com.prumo.project.dto.AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.addMember(id, request));
    }

    @DeleteMapping("/{id}/members/{userId}")
    // Permitir se o requisitante for MANAGER do projeto OU se for o próprio membro pedindo pra sair (auto-remoção)
    @PreAuthorize("@projectSecurity.isManager(authentication, #id) or @projectSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }
}


