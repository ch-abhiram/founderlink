package com.auth_service.Service;

import com.auth_service.DTO.CreateUserRequest;
import feign.FeignException;
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

    public void createUserProfile(String email, String role) {
        try {
            userClient.getUser(email);
            log.info("User profile already exists in user-service for email={}", email);
            return;
        } catch (FeignException.NotFound ex) {
            String derivedName = deriveDisplayName(email);
            userClient.createUser(new CreateUserRequest(email, derivedName, role));
            log.info("User profile created in user-service for email={}", email);
            return;
        }
    }

    public String fallback(String email, Exception ex) {
        log.warn("User service circuit breaker triggered for email={}: {}", email, ex.getMessage());
        return "USER";
    }

    private String deriveDisplayName(String email) {
        String localPart = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : "user";
        return localPart.trim().isEmpty() ? "user" : localPart;
    }
}
