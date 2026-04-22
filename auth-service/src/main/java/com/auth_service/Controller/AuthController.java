package com.auth_service.Controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth_service.DTO.LoginRequest;
import com.auth_service.DTO.LoginResponse;
import com.auth_service.DTO.RefreshRequest;
import com.auth_service.DTO.ResendOtpRequest;
import com.auth_service.DTO.RegisterRequest;
import com.auth_service.DTO.RegisterResponse;
import com.auth_service.DTO.VerifyOtpRequest;
import com.auth_service.DTO.VerificationResponse;
import com.auth_service.Exception.UnauthorizedException;
import com.auth_service.Service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @GetMapping("/verify")
    public VerificationResponse verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token);
    }

    @PostMapping("/verify-otp")
    public VerificationResponse verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        return authService.verifyOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestBody @Valid ResendOtpRequest request) {
        authService.resendOtp(request.getEmail());
        return "A new verification code has been sent to your email.";
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }
    
    @PostMapping("/logout")
    public String logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        authService.logout(token);
        return "Logged out successfully";
    }
}
