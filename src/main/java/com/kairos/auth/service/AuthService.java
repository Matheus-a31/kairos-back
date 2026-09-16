package com.kairos.auth.service;

import com.kairos.auth.model.RefreshToken;
import com.kairos.auth.model.Role;
import com.kairos.auth.model.User;
import com.kairos.auth.dto.AuthResponse;
import com.kairos.auth.dto.LoginRequest;
import com.kairos.auth.dto.RegisterRequest;
import com.kairos.auth.dto.TokenRefreshRequest;
import com.kairos.auth.repository.RefreshTokenRepository;
import com.kairos.auth.repository.UserRepository;
import com.kairos.auth.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${kairos.jwt.access-expiration}")
    private long jwtExpirationInMs;
    
    @Value("${kairos.jwt.refresh-expiration}")
    private long refreshExpirationInMs;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.MANAGER // Alterado para MANAGER temporariamente para facilitar os testes de criação de projeto
        );

        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), jwtExpirationInMs / 1000);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        return refreshTokenRepository.findByToken(request.refreshToken())
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtTokenProvider.generateTokenFromUsername(user.getEmail());
                    return new AuthResponse(accessToken, request.refreshToken(), jwtExpirationInMs / 1000);
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }

    private RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user); // Invalidates previous tokens
        
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plus(refreshExpirationInMs, ChronoUnit.MILLIS),
                user
        );
        
        return refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}


