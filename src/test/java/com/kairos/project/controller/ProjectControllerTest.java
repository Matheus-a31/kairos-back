package com.kairos.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kairos.core.exceptions.GlobalExceptionHandler;
import com.kairos.project.dto.AddMemberRequest;
import com.kairos.project.dto.ProjectRequest;
import com.kairos.project.dto.ProjectResponse;
import com.kairos.project.model.ProjectRole;
import com.kairos.project.model.ProjectStatus;
import com.kairos.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createProject_ShouldReturnCreated() throws Exception {
        ProjectRequest request = new ProjectRequest("Proj", "Desc", LocalDate.now(), LocalDate.now().plusDays(10), ProjectStatus.PLANNING);
        ProjectResponse response = new ProjectResponse(1L, "Proj", "Desc", LocalDate.now(), LocalDate.now().plusDays(10), ProjectStatus.PLANNING);

        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Proj"));
    }

    @Test
    void getProjects_ShouldReturnOk() throws Exception {
        ProjectResponse response = new ProjectResponse(1L, "Proj", "Desc", LocalDate.now(), LocalDate.now().plusDays(10), ProjectStatus.PLANNING);
        Page<ProjectResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(projectService.getProjects(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Proj"));
    }
}
