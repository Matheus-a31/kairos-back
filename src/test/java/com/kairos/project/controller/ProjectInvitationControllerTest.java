package com.kairos.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.exceptions.GlobalExceptionHandler;
import com.kairos.project.dto.InvitationRequest;
import com.kairos.project.model.ProjectRole;
import com.kairos.project.service.ProjectInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProjectInvitationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectInvitationService invitationService;

    @InjectMocks
    private ProjectInvitationController invitationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(invitationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createInvitation_ShouldReturnOk() throws Exception {
        InvitationRequest request = new InvitationRequest("test@example.com", ProjectRole.MEMBER);
        doNothing().when(invitationService).createInvitation(eq(1L), any(InvitationRequest.class));

        mockMvc.perform(post("/api/projects/1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(invitationService, times(1)).createInvitation(eq(1L), any(InvitationRequest.class));
    }

    @Test
    void acceptInvitation_ShouldReturnOk() throws Exception {
        doNothing().when(invitationService).acceptInvitation("token123");

        mockMvc.perform(post("/api/projects/invitations/token123/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(invitationService, times(1)).acceptInvitation("token123");
    }
}
