package com.startup_service.Service;

import org.springframework.stereotype.Service;

import com.startup_service.DTO.UserDto;
import com.startup_service.Feign.UserClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Wrapper {

    private final UserClient userClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUser")
    public UserDto fetchUser(String email) {
        return userClient.getUser(email);
    }

    public UserDto fallbackUser(String email, Throwable ex) {
        log.warn("User service fallback triggered for email={}: {}", email, ex.getMessage());
        return new UserDto(email, "Unknown", "USER");
    }
}
