package com.auth_service.Service;

import com.auth_service.Entity.RefreshToken;
import com.auth_service.Entity.User;
import com.auth_service.Exception.ConflictException;
import com.auth_service.Exception.ForbiddenOperationException;
import com.auth_service.DTO.VerificationResponse;
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

    @Mock
    private OtpEmailService otpEmailService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole("ROLE_FOUNDER");
        mockUser.setEmailVerified(true);
    }

    @Test
    void testRegister() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        RegisterResponse response = authService.register("test@test.com", "password", "ROLE_FOUNDER");

        assertEquals("User registered successfully. Please check your email for a 6-digit verification code.", response.getMessage());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("ROLE_FOUNDER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
        verify(redisService).storeOtp(eq("test@test.com"), anyString(), anyLong());
        verify(otpEmailService).sendOtp(eq("test@test.com"), anyString());
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
        verify(refreshTokenRepository).deleteByToken("mockRefreshToken");
    }

    @Test
    void testRefreshTokenRotatesOnSuccess() {
        RefreshToken token = new RefreshToken();
        token.setToken("mockRefreshToken");
        token.setEmail("test@test.com");
        token.setExpiryDate(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByToken("mockRefreshToken")).thenReturn(Optional.of(token));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(userServiceClientWrapper.getUserRole("test@test.com")).thenReturn("ROLE_FOUNDER");
        when(jwtUtil.generateToken("test@test.com", "ROLE_FOUNDER")).thenReturn("newAccessToken");

        LoginResponse response = authService.refreshToken("mockRefreshToken");

        assertEquals("newAccessToken", response.getAccessToken());
        assertNotEquals("mockRefreshToken", response.getRefreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void testLoginEmailNotVerified() {
        mockUser.setEmailVerified(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("test@test.com", "password");
        });

        assertEquals("Email not verified. Please verify your email before logging in.", exception.getMessage());
    }

    @Test
    void testVerifyEmailSuccess() {
        mockUser.setVerificationToken("token123");
        mockUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
        when(userRepository.findByVerificationToken("token123")).thenReturn(Optional.of(mockUser));

        var response = authService.verifyEmail("token123");

        assertEquals("Email verified successfully", response.getMessage());
        assertEquals("test@test.com", response.getEmail());
        assertTrue(mockUser.getEmailVerified());
        verify(userRepository).save(mockUser);
    }

    @Test
    void testVerifyEmailExpiredToken() {
        mockUser.setVerificationToken("token123");
        mockUser.setVerificationTokenExpiry(LocalDateTime.now().minusHours(1));
        when(userRepository.findByVerificationToken("token123")).thenReturn(Optional.of(mockUser));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyEmail("token123");
        });

        assertEquals("Invalid or expired verification token", exception.getMessage());
    }

    @Test
    void testVerifyEmailInvalidToken() {
        when(userRepository.findByVerificationToken("missing-token")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyEmail("missing-token");
        });

        assertEquals("Invalid or expired verification token", exception.getMessage());
    }

    @Test
    void testLogoutBlacklistsToken() {
        when(jwtUtil.getRemainingTime("access-token")).thenReturn(12345L);

        authService.logout("access-token");

        verify(redisService).blacklistToken("access-token", 12345L);
    }

    @Test
    void testVerifyOtpSuccess() {
        mockUser.setEmailVerified(false);
        when(redisService.getOtp("test@test.com")).thenReturn("123456");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        VerificationResponse response = authService.verifyOtp("test@test.com", "123456");

        assertEquals("Email verified successfully", response.getMessage());
        assertTrue(mockUser.getEmailVerified());
        verify(redisService).deleteOtp("test@test.com");
    }

    @Test
    void testVerifyOtpWrongOtpThrowsForbidden() {
        when(redisService.getOtp("test@test.com")).thenReturn("999999");

        assertThrows(ForbiddenOperationException.class,
                () -> authService.verifyOtp("test@test.com", "123456"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void testVerifyOtpExpiredOtpThrowsForbidden() {
        when(redisService.getOtp("test@test.com")).thenReturn(null);

        assertThrows(ForbiddenOperationException.class,
                () -> authService.verifyOtp("test@test.com", "123456"));
    }

    @Test
    void testResendOtpSuccess() {
        mockUser.setEmailVerified(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        authService.resendOtp("test@test.com");

        verify(redisService).storeOtp(eq("test@test.com"), anyString(), anyLong());
        verify(otpEmailService).sendOtp(eq("test@test.com"), anyString());
    }

    @Test
    void testResendOtpAlreadyVerifiedThrowsConflict() {
        mockUser.setEmailVerified(true);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        assertThrows(ConflictException.class,
                () -> authService.resendOtp("test@test.com"));
    }

    @Test
    void testRegisterSendOtpCalledOnce() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        authService.register("test@test.com", "password", "ROLE_FOUNDER");

        verify(redisService).storeOtp(eq("test@test.com"), anyString(), anyLong());
        verify(otpEmailService).sendOtp(eq("test@test.com"), anyString());
    }
}
