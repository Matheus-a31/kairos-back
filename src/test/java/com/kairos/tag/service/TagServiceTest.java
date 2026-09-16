package com.kairos.tag.service;

import com.kairos.core.exceptions.ResourceNotFoundException;
import com.kairos.project.model.Project;
import com.kairos.project.repository.ProjectRepository;
import com.kairos.tag.dto.TagRequest;
import com.kairos.tag.dto.TagResponse;
import com.kairos.tag.model.Tag;
import com.kairos.tag.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TagService tagService;

    private Project project;
    private Tag tag;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setName("Test Project");
        ReflectionTestUtils.setField(project, "id", 1L);

        tag = new Tag(project, "Frontend", "#00F");
        ReflectionTestUtils.setField(tag, "id", 1L);
    }

    @Test
    void getProjectTags_ShouldReturnList() {
        when(tagRepository.findByProjectId(1L)).thenReturn(List.of(tag));

        List<TagResponse> tags = tagService.getProjectTags(1L);

        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals("Frontend", tags.get(0).name());
        assertEquals("#00F", tags.get(0).color());
    }

    @Test
    void createTag_ShouldReturnTag() {
        TagRequest request = new TagRequest("Backend", "#F00");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(tagRepository.findByProjectIdAndName(1L, "Backend")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag t = invocation.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 2L);
            return t;
        });

        TagResponse response = tagService.createTag(1L, request);

        assertNotNull(response);
        assertEquals("Backend", response.name());
        assertEquals("#F00", response.color());
        assertEquals(2L, response.id());
    }

    @Test
    void createTag_ShouldThrowException_WhenTagAlreadyExists() {
        TagRequest request = new TagRequest("Frontend", "#F00");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(tagRepository.findByProjectIdAndName(1L, "Frontend")).thenReturn(Optional.of(tag));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> tagService.createTag(1L, request));
        assertEquals("Já existe uma tag com este nome neste projeto", exception.getMessage());
    }

    @Test
    void deleteTag_ShouldDelete() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        assertDoesNotThrow(() -> tagService.deleteTag(1L, 1L));

        verify(tagRepository, times(1)).delete(tag);
    }

    @Test
    void deleteTag_ShouldThrowException_WhenTagNotFromProject() {
        Project otherProject = new Project();
        ReflectionTestUtils.setField(otherProject, "id", 2L);
        tag.setProject(otherProject);

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> tagService.deleteTag(1L, 1L));
        assertEquals("Tag não pertence a este projeto", exception.getMessage());
        
        verify(tagRepository, never()).delete(any());
    }
}
