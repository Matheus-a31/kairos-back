package com.kairos.web;

import com.kairos.dto.TagRequest;
import com.kairos.dto.TagResponse;
import com.kairos.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tags")
@PreAuthorize("@projectSecurity.isMemberOrManager(authentication, #projectId)")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> getTags(@PathVariable Long projectId) {
        return ResponseEntity.ok(tagService.getProjectTags(projectId));
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.isManager(authentication, #projectId)")
    public ResponseEntity<TagResponse> createTag(
            @PathVariable Long projectId,
            @Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(projectId, request));
    }

    @DeleteMapping("/{tagId}")
    @PreAuthorize("@projectSecurity.isManager(authentication, #projectId)")
    public ResponseEntity<Void> deleteTag(@PathVariable Long projectId, @PathVariable Long tagId) {
        tagService.deleteTag(projectId, tagId);
        return ResponseEntity.noContent().build();
    }
}
