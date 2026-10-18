package com.prumo.tag.service;

import com.prumo.project.model.Project;
import com.prumo.tag.model.Tag;
import com.prumo.tag.dto.TagRequest;
import com.prumo.tag.dto.TagResponse;
import com.prumo.core.exceptions.ResourceNotFoundException;
import com.prumo.project.repository.ProjectRepository;
import com.prumo.tag.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ProjectRepository projectRepository;

    public TagService(TagRepository tagRepository, ProjectRepository projectRepository) {
        this.tagRepository = tagRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getProjectTags(Long projectId) {
        return tagRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagResponse createTag(Long projectId, TagRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
                
        tagRepository.findByProjectIdAndName(projectId, request.name()).ifPresent(t -> {
            throw new IllegalArgumentException("Já existe uma tag com este nome neste projeto");
        });

        Tag tag = new Tag(project, request.name(), request.color());
        return toResponse(tagRepository.save(tag));
    }
    
    @Transactional
    public void deleteTag(Long projectId, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag não encontrada"));
                
        if (!tag.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Tag não pertence a este projeto");
        }
        
        tagRepository.delete(tag);
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColor());
    }
}


