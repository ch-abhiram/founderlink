package com.auth_service.Service;

import org.springframework.stereotype.Service;

import com.auth_service.Feign.UserClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClientWrapper {

    private final UserClient userClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallback")
    public String getUserRole(String email) {
        return userClient.getUser(email).getRole();
    }

    public String fallback(String email, Exception ex) {
        log.warn("User service circuit breaker triggered for email={}: {}", email, ex.getMessage());
        return "USER";
    }
}
