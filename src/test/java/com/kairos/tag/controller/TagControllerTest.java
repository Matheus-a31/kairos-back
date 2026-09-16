package com.kairos.tag.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.exceptions.GlobalExceptionHandler;
import com.kairos.tag.dto.TagRequest;
import com.kairos.tag.dto.TagResponse;
import com.kairos.tag.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(tagController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getTags_ShouldReturnOk() throws Exception {
        TagResponse response = new TagResponse(1L, "Backend", "#00F");

        when(tagService.getProjectTags(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/projects/1/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Backend"))
                .andExpect(jsonPath("$[0].color").value("#00F"));
    }

    @Test
    void createTag_ShouldReturnCreated() throws Exception {
        TagRequest request = new TagRequest("Backend", "#00F");
        TagResponse response = new TagResponse(1L, "Backend", "#00F");

        when(tagService.createTag(eq(1L), any(TagRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Backend"));
    }

    @Test
    void deleteTag_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/projects/1/tags/1"))
                .andExpect(status().isNoContent());

        verify(tagService, times(1)).deleteTag(1L, 1L);
    }
}
