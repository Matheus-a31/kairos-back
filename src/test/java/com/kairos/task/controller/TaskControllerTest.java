package com.kairos.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kairos.core.exceptions.GlobalExceptionHandler;
import com.kairos.task.dto.TaskRequest;
import com.kairos.task.dto.TaskResponse;
import com.kairos.task.model.TaskPriority;
import com.kairos.task.model.TaskStatus;
import com.kairos.task.service.TaskService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTask_ShouldReturnCreated() throws Exception {
        TaskRequest request = new TaskRequest("Task 1", "Desc", TaskPriority.HIGH, LocalDate.now(), 1L, Set.of());
        TaskResponse response = new TaskResponse(1L, "Task 1", "Desc", TaskPriority.HIGH, TaskStatus.TODO, LocalDate.now(), 1L, "User", null, null, null);

        when(taskService.createTask(eq(1L), any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Task 1"));
    }

    @Test
    void getTasks_ShouldReturnOk() throws Exception {
        TaskResponse response = new TaskResponse(1L, "Task 1", "Desc", TaskPriority.HIGH, TaskStatus.TODO, LocalDate.now(), 1L, "User", null, null, null);
        Page<TaskResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(taskService.getTasks(eq(1L), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Task 1"));
    }

    @Test
    void changeStatus_ShouldReturnOk() throws Exception {
        TaskResponse response = new TaskResponse(1L, "Task 1", "Desc", TaskPriority.HIGH, TaskStatus.IN_PROGRESS, LocalDate.now(), 1L, "User", null, null, null);
        
        when(taskService.changeStatus(eq(1L), eq(1L), eq(TaskStatus.IN_PROGRESS), any())).thenReturn(response);

        String jsonRequest = "{\"status\":\"IN_PROGRESS\", \"comment\":\"Starting task\"}";

        mockMvc.perform(patch("/api/projects/1/tasks/1/status")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
