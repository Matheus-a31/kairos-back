package com.kairos.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.exceptions.GlobalExceptionHandler;
import com.kairos.project.dto.KanbanColumnRequest;
import com.kairos.project.dto.KanbanColumnResponse;
import com.kairos.project.service.KanbanColumnService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class KanbanColumnControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KanbanColumnService columnService;

    @InjectMocks
    private KanbanColumnController columnController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(columnController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getColumns_ShouldReturnOk() throws Exception {
        KanbanColumnResponse response = new KanbanColumnResponse(1L, 1L, "To Do", "#FFF", 0, false);
        when(columnService.getColumns(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/projects/1/columns")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("To Do"));
    }

    @Test
    void createColumn_ShouldReturnCreated() throws Exception {
        KanbanColumnRequest request = new KanbanColumnRequest("Doing", "#FFF", 1);
        KanbanColumnResponse response = new KanbanColumnResponse(2L, 1L, "Doing", "#FFF", 1, false);

        when(columnService.createColumn(eq(1L), any(KanbanColumnRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects/1/columns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Doing"));
    }
}
