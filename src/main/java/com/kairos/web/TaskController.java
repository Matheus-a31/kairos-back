package com.kairos.web;

import com.kairos.domain.TaskPriority;
import com.kairos.domain.TaskStatus;
import com.kairos.dto.TaskRequest;
import com.kairos.dto.TaskResponse;
import com.kairos.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@PreAuthorize("@projectSecurity.isMemberOrManager(authentication, #projectId)")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(projectId, search, status, priority, assigneeId, pageable));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTask(projectId, taskId));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, request));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody com.kairos.dto.ChangeStatusRequest request) {
        return ResponseEntity.ok(taskService.changeStatus(projectId, taskId, request.status(), request.comment()));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("@projectSecurity.isManager(authentication, #projectId)")
    public ResponseEntity<Void> deleteTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(projectId, taskId);
        return ResponseEntity.noContent().build();
    }
}
