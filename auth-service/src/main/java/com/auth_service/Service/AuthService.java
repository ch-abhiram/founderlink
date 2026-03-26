package com.auth_service.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth_service.DTO.LoginResponse;
import com.auth_service.DTO.RegisterResponse;
import com.auth_service.DTO.VerificationResponse;
import com.auth_service.Entity.RefreshToken;
import com.auth_service.Entity.User;
import com.auth_service.Repository.RefreshTokenRepository;
import com.auth_service.Repository.UserRepository;
import com.auth_service.Util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final EventPublisher eventPublisher;
    private final RedisService redisService;
    private final UserServiceClientWrapper userServiceClientWrapper;

    public RegisterResponse register(String email, String password, String role) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        String verificationToken = UUID.randomUUID().toString();

        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEmailVerified(false);
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        try {
            userServiceClientWrapper.createUserProfile(normalizedEmail, role);
        } catch (Exception ex) {
            // Keep auth registration resilient even if user-service is down or user already exists.
            log.warn("User profile sync to user-service failed for email={}: {}", normalizedEmail, ex.getMessage());
        }

        log.info("Email verification token generated for email={}: {}", normalizedEmail, verificationToken);

        return new RegisterResponse(
                "User registered successfully. Please verify your email before logging in.",
                normalizedEmail,
                role
        );
    }

    public LoginResponse login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email not verified. Please verify your email before logging in.");
        }

        String role = resolveRole(normalizedEmail, user.getRole());
        String accessToken = jwtUtil.generateToken(normalizedEmail, role);
        String refreshToken = createRefreshToken(normalizedEmail);

        eventPublisher.publishUserLogin(normalizedEmail);

        return new LoginResponse(accessToken, refreshToken, normalizedEmail, role);
    }

    public String createRefreshToken(String email) {
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public LoginResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = userRepository.findByEmail(token.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = resolveRole(token.getEmail(), user.getRole());
        String newAccessToken = jwtUtil.generateToken(token.getEmail(), role);

        return new LoginResponse(newAccessToken, refreshToken, token.getEmail(), role);
    }

    public VerificationResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        if (user.getVerificationTokenExpiry() == null
                || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired verification token");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        return new VerificationResponse("Email verified successfully", user.getEmail());
    }

    public void logout(String token) {
        long expiry = jwtUtil.getRemainingTime(token);
        redisService.blacklistToken(token, expiry);
    }

    private String resolveRole(String email, String authRole) {
        try {
            String userServiceRole = userServiceClientWrapper.getUserRole(email);
            if (userServiceRole != null
                    && !userServiceRole.isBlank()
                    && !"USER".equalsIgnoreCase(userServiceRole)) {
                return userServiceRole;
            }
        } catch (Exception e) {
            log.warn("User service role lookup failed for email={}, using auth-service role", email);
        }

        if (authRole != null && !authRole.isBlank()) {
            return authRole;
        }

        return "USER";
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
