package com.auth_service.Service;

import com.auth_service.Entity.RefreshToken;
import com.auth_service.Entity.User;
import com.auth_service.Repository.RefreshTokenRepository;
import com.auth_service.Repository.UserRepository;
import com.auth_service.Util.JwtUtil;
import com.auth_service.DTO.LoginResponse;
import com.auth_service.DTO.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private RedisService redisService;

    @Mock
    private UserServiceClientWrapper userServiceClientWrapper;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole("ROLE_FOUNDER");
    }

    @Test
    void testRegister() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        RegisterResponse response = authService.register("test@test.com", "password", "ROLE_FOUNDER");

        assertEquals("User registered successfully. Please verify your email before logging in.", response.getMessage());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("ROLE_FOUNDER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginHappyPath() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(userServiceClientWrapper.getUserRole("test@test.com")).thenReturn("ROLE_FOUNDER");
        when(jwtUtil.generateToken("test@test.com", "ROLE_FOUNDER")).thenReturn("mockAccessToken");

        LoginResponse response = authService.login("test@test.com", "password");

        assertNotNull(response);
        assertEquals("mockAccessToken", response.getAccessToken());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("ROLE_FOUNDER", response.getRole());
        verify(eventPublisher, times(1)).publishUserLogin("test@test.com");
    }

    @Test
    void testLoginWrongPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("test@test.com", "wrongpassword");
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testRefreshTokenExpired() {
        RefreshToken token = new RefreshToken();
        token.setToken("mockRefreshToken");
        token.setExpiryDate(LocalDateTime.now().minusDays(1)); // expired

        when(refreshTokenRepository.findByToken("mockRefreshToken")).thenReturn(Optional.of(token));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken("mockRefreshToken");
        });

        assertEquals("Refresh token expired", exception.getMessage());
    }
}
