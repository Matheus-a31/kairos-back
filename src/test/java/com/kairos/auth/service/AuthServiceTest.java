package com.kairos.auth.service;

import com.kairos.auth.dto.AuthResponse;
import com.kairos.auth.dto.LoginRequest;
import com.kairos.auth.dto.RegisterRequest;
import com.kairos.auth.dto.TokenRefreshRequest;
import com.kairos.auth.model.RefreshToken;
import com.kairos.auth.model.Role;
import com.kairos.auth.model.User;
import com.kairos.auth.repository.RefreshTokenRepository;
import com.kairos.auth.repository.UserRepository;
import com.kairos.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private com.kairos.project.service.ProjectInvitationService projectInvitationService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpirationInMs", 900000L);
        ReflectionTestUtils.setField(authService, "refreshExpirationInMs", 604800000L);
    }

    @Test
    void register_ShouldSaveUser_WhenEmailIsNotTaken() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");

        // Act
        authService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("Test User", savedUser.getName());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encoded_password", savedUser.getPassword());
        assertEquals(Role.MANAGER, savedUser.getRole()); // Asserção no comportamento atual
        
        verify(projectInvitationService, times(1)).processPendingInvitations(savedUser);
    }

    @Test
    void register_ShouldThrowException_WhenEmailIsTaken() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        Authentication authentication = mock(Authentication.class);
        User user = new User("Test User", "test@example.com", "encoded_password", Role.MANAGER);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("access_token");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        
        // Mock the RefreshToken creation
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            return token;
        });

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertNotNull(response.refreshToken());
        assertEquals(900L, response.expiresIn());
        
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenRefreshTokenIsValid() {
        // Arrange
        String oldRefreshToken = UUID.randomUUID().toString();
        TokenRefreshRequest request = new TokenRefreshRequest(oldRefreshToken);
        User user = new User("Test User", "test@example.com", "encoded_password", Role.MANAGER);
        RefreshToken validToken = new RefreshToken(oldRefreshToken, LocalDateTime.now().plusDays(1), user);
        
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(validToken));
        when(jwtTokenProvider.generateTokenFromUsername(user.getEmail())).thenReturn("new_access_token");

        // Act
        AuthResponse response = authService.refreshToken(request);

        // Assert
        assertNotNull(response);
        assertEquals("new_access_token", response.accessToken());
        assertEquals(oldRefreshToken, response.refreshToken());
        assertEquals(900L, response.expiresIn());
    }

    @Test
    void refreshToken_ShouldThrowExceptionAndDeleteToken_WhenRefreshTokenIsExpired() {
        // Arrange
        String expiredRefreshToken = UUID.randomUUID().toString();
        TokenRefreshRequest request = new TokenRefreshRequest(expiredRefreshToken);
        User user = new User("Test User", "test@example.com", "encoded_password", Role.MANAGER);
        RefreshToken expiredToken = new RefreshToken(expiredRefreshToken, LocalDateTime.now().minusDays(1), user);
        
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(request));
        assertEquals("Refresh token was expired. Please make a new signin request", exception.getMessage());
        
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
        verify(jwtTokenProvider, never()).generateTokenFromUsername(anyString());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenRefreshTokenIsNotFound() {
        // Arrange
        TokenRefreshRequest request = new TokenRefreshRequest("invalid-token");
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(request));
        assertEquals("Invalid refresh token", exception.getMessage());
    }
}
