package com.startup_service.Service;

import org.springframework.stereotype.Service;

import com.startup_service.DTO.UserDto;
import com.startup_service.Feign.UserClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Wrapper {

    private final UserClient userClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUser")
    public UserDto fetchUser(String email) {
        return userClient.getUser(email);
    }

    // ✅ Fallback method MUST match signature
    public UserDto fallbackUser(String email, Throwable ex) {
        System.out.println("🔥 Circuit Breaker Triggered for email: " + email);
        System.out.println("Reason: " + ex.getMessage());

        // return safe default user
        return new UserDto(email, "Unknown", "USER");
    }
}
