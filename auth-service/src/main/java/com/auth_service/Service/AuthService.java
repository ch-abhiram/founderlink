package com.auth_service.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth_service.Entity.RefreshToken;
import com.auth_service.Entity.User;
import com.auth_service.DTO.LoginResponse;
import com.auth_service.DTO.RegisterResponse;
import com.auth_service.Feign.UserClient;
import com.auth_service.Repository.RefreshTokenRepository;
import com.auth_service.Repository.UserRepository;
import com.auth_service.Util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final EventPublisher eventPublisher;
    private final RedisService redisService;
    private final UserServiceClientWrapper userServiceClientWrapper;

    public RegisterResponse register(String email, String password, String role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);
        return new RegisterResponse("User registered successfully", email, role);
    }

    public LoginResponse login(String email, String password) {

        // Step 1: Validate credentials
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Step 2: Fetch role from user-service
        String role;

        try {
        	role = userServiceClientWrapper.getUserRole(email);
        } catch (Exception e) {
            System.out.println("User service failed, defaulting role");
            role = "USER";
        }

        // Step 3: Generate JWT
        String accessToken = jwtUtil.generateToken(email, role != null ? role : "USER");

        // Step 4: Generate refresh token
        String refreshToken = createRefreshToken(email);

        // 🔥 STEP 5: PUBLISH EVENT (ADD THIS)
        eventPublisher.publishUserLogin(email);

        // Step 6: Return tokens
        return new LoginResponse(accessToken, refreshToken, email, role != null ? role : "USER");
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

        // Step 1: Find token in DB
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Step 2: Check expiry
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        // Fetch role
        String role;
        try {
            role = userServiceClientWrapper.getUserRole(token.getEmail());
        } catch (Exception e) {
            role = "USER";
        }

        // Step 3: Generate new access token
        String newAccessToken = jwtUtil.generateToken(token.getEmail(), role != null ? role : "USER");

        // Step 4: Return new token
        return new LoginResponse(newAccessToken, refreshToken, token.getEmail(), role != null ? role : "USER");
    }
    
    public void logout(String token) {

        long expiry = jwtUtil.getRemainingTime(token);

        redisService.blacklistToken(token, expiry);

        // also delete refresh tokens if needed
    }
    
    
}
