package com.kairos.web;

import com.kairos.dto.KanbanColumnRequest;
import com.kairos.dto.KanbanColumnResponse;
import com.kairos.service.KanbanColumnService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/columns")
public class KanbanColumnController {

    private final KanbanColumnService columnService;

    public KanbanColumnController(KanbanColumnService columnService) {
        this.columnService = columnService;
    }

    @GetMapping
    public ResponseEntity<List<KanbanColumnResponse>> getColumns(@PathVariable Long projectId) {
        return ResponseEntity.ok(columnService.getColumns(projectId));
    }

    @PostMapping
    public ResponseEntity<KanbanColumnResponse> createColumn(
            @PathVariable Long projectId,
            @RequestBody KanbanColumnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(columnService.createColumn(projectId, request));
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<KanbanColumnResponse> updateColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId,
            @RequestBody KanbanColumnRequest request) {
        return ResponseEntity.ok(columnService.updateColumn(projectId, columnId, request));
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId) {
        columnService.deleteColumn(projectId, columnId);
        return ResponseEntity.noContent().build();
    }
}
