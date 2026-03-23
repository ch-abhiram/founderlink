package com.auth_service.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth_service.DTO.AuthRequest;
import com.auth_service.Service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request) {
        return authService.register(
                request.getEmail(),
                request.getPassword()
        );
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {
        return authService.login(
                request.getEmail(),
                request.getPassword()
        );
    }
    
    @GetMapping("/test")
    public String test() {
        return "Auth working";
    }
}
