package com.auth_service.Service;

import org.springframework.stereotype.Service;

import com.auth_service.Feign.UserClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceClientWrapper {

    private final UserClient userClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallback")
    public String getUserRole(String email) {
        return userClient.getUser(email).getRole();
    }

    public String fallback(String email, Exception ex) {
        System.out.println("🔥 Circuit Breaker Triggered: " + ex.getMessage());
        return "USER";
    }
}
