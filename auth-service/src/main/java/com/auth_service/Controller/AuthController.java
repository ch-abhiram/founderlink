package com.auth_service.Controller;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth_service.DTO.AuthRequest;
import com.auth_service.DTO.LoginResponse;
import com.auth_service.DTO.RegisterResponse;
import com.auth_service.DTO.VerificationResponse;
import com.auth_service.Service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody @Valid AuthRequest request) {
        return authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid AuthRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @GetMapping("/verify")
    public VerificationResponse verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token);
    }
    
    @GetMapping("/test")
    public String test() {
        return "Auth working";
    }
    
    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody Map<String, String> request) {
        return authService.refreshToken(request.get("refreshToken"));
    }
    
    @PostMapping("/logout")
    public String logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        authService.logout(token);
        return "Logged out successfully";
    }
}
