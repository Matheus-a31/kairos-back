package com.kairos.web;

import com.kairos.dto.ProjectRequest;
import com.kairos.dto.ProjectResponse;
import com.kairos.service.ProjectService;
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

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectSecurity.isManager(authentication, #id)")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints de Membros --- //

    @GetMapping("/{id}/members")
    @PreAuthorize("@projectSecurity.isMemberOrManager(authentication, #id)")
    public ResponseEntity<Page<com.kairos.dto.ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectMembers(id, pageable));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("@projectSecurity.isManager(authentication, #id)")
    public ResponseEntity<com.kairos.dto.ProjectMemberResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody com.kairos.dto.AddMemberRequest request) {
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
