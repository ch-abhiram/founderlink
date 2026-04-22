package com.auth_service.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth_service.Exception.ConflictException;
import com.auth_service.Exception.ForbiddenOperationException;
import com.auth_service.DTO.LoginResponse;
import com.auth_service.DTO.RegisterResponse;
import com.auth_service.DTO.VerificationResponse;
import com.auth_service.Entity.RefreshToken;
import com.auth_service.Exception.ResourceNotFoundException;
import com.auth_service.Exception.UnauthorizedException;
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
    private final OtpEmailService otpEmailService;

    @Value("${founderlink.otp.expiry-minutes:10}")
    private long otpExpiryMinutes;

    public RegisterResponse register(String email, String password, String role) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ConflictException("User already exists");
        }

        User user = new User();

        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEmailVerified(false);
        userRepository.save(user);

        String otp = generateOtp();
        redisService.storeOtp(normalizedEmail, otp, otpExpiryMinutes);
        otpEmailService.sendOtp(normalizedEmail, otp);

        try {
            userServiceClientWrapper.createUserProfile(normalizedEmail, role);
        } catch (Exception ex) {
            // Keep auth registration resilient even if user-service is down or user already exists.
            log.warn("User profile sync to user-service failed for email={}: {}", normalizedEmail, ex.getMessage());
        }

        return new RegisterResponse(
                "User registered successfully. Please check your email for a 6-digit verification code.",
                normalizedEmail,
                role
        );
    }

    public LoginResponse login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new ForbiddenOperationException("Email not verified. Please verify your email before logging in.");
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
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = userRepository.findByEmail(token.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String role = resolveRole(token.getEmail(), user.getRole());
        String newAccessToken = jwtUtil.generateToken(token.getEmail(), role);
        String newRefreshToken = createRefreshToken(token.getEmail());
        refreshTokenRepository.delete(token);

        return new LoginResponse(newAccessToken, newRefreshToken, token.getEmail(), role);
    }

    public VerificationResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ForbiddenOperationException("Invalid or expired verification token"));

        if (user.getVerificationTokenExpiry() == null
                || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ForbiddenOperationException("Invalid or expired verification token");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        return new VerificationResponse("Email verified successfully", user.getEmail());
    }

    public VerificationResponse verifyOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);

        String storedOtp = redisService.getOtp(normalizedEmail);
        if (storedOtp == null) {
            throw new ForbiddenOperationException("OTP has expired or was never issued. Please request a new one.");
        }

        if (!storedOtp.equals(otp)) {
            throw new ForbiddenOperationException("Invalid OTP. Please check the code and try again.");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        redisService.deleteOtp(normalizedEmail);

        return new VerificationResponse("Email verified successfully", normalizedEmail);
    }

    public void resendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ConflictException("Email is already verified.");
        }

        String otp = generateOtp();
        redisService.storeOtp(normalizedEmail, otp, otpExpiryMinutes);
        otpEmailService.sendOtp(normalizedEmail, otp);
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

    private String generateOtp() {
        int otp = 100_000 + new Random().nextInt(900_000);
        return String.valueOf(otp);
    }
}
