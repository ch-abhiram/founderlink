package com.auth_service.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.auth_service.DTO.CreateUserRequest;
import com.auth_service.DTO.UserResponse;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/users/{email:.+}")
    UserResponse getUser(@PathVariable String email);

    @PostMapping("/users")
    void createUser(@RequestBody CreateUserRequest request);
}
