package com.kairos.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.auth.dto.AuthResponse;
import com.kairos.auth.dto.LoginRequest;
import com.kairos.auth.dto.RegisterRequest;
import com.kairos.auth.dto.TokenRefreshRequest;
import com.kairos.auth.service.AuthService;
import com.kairos.core.exceptions.GlobalExceptionHandler;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Setup manual do MockMvc para rodar testes isolados de controllers sem subir o contexto do Spring Security completo
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        doNothing().when(authService).register(any(RegisterRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmailAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        doThrow(new IllegalArgumentException("Email already in use")).when(authService).register(any(RegisterRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        AuthResponse response = new AuthResponse("access_token", "refresh_token", 900L);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void refresh_ShouldReturnNewAuthResponse_WhenRefreshTokenIsValid() throws Exception {
        // Arrange
        TokenRefreshRequest request = new TokenRefreshRequest("valid_refresh_token");
        AuthResponse response = new AuthResponse("new_access_token", "valid_refresh_token", 900L);
        when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access_token"))
                .andExpect(jsonPath("$.refreshToken").value("valid_refresh_token"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(authService, times(1)).refreshToken(any(TokenRefreshRequest.class));
    }

    @Test
    void refresh_ShouldReturnBadRequest_WhenRefreshTokenIsInvalid() throws Exception {
        // Arrange
        TokenRefreshRequest request = new TokenRefreshRequest("invalid_refresh_token");
        doThrow(new IllegalArgumentException("Invalid refresh token")).when(authService).refreshToken(any(TokenRefreshRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
}
