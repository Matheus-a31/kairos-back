package com.prumo.tag.controller;
import com.prumo.project.model.Project;
import com.prumo.tag.model.Tag;
import com.prumo.project.security.ProjectSecurity;


import com.prumo.tag.dto.TagRequest;
import com.prumo.tag.dto.TagResponse;
import com.prumo.tag.service.TagService;
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
    @PreAuthorize("@projectSecurity.isMemberOrManager(authentication, #projectId)")
    public ResponseEntity<TagResponse> createTag(
            @PathVariable Long projectId,
            @Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(projectId, request));
    }

    @PutMapping("/{tagId}")
    @PreAuthorize("@projectSecurity.isMemberOrManager(authentication, #projectId)")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long projectId,
            @PathVariable Long tagId,
            @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.updateTag(projectId, tagId, request));
    }

    @DeleteMapping("/{tagId}")
    @PreAuthorize("@projectSecurity.isMemberOrManager(authentication, #projectId)")
    public ResponseEntity<Void> deleteTag(@PathVariable Long projectId, @PathVariable Long tagId) {
        tagService.deleteTag(projectId, tagId);
        return ResponseEntity.noContent().build();
    }
}


