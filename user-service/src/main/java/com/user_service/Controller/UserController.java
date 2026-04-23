package com.user_service.Controller;

import org.springframework.http.ResponseEntity;
import java.net.URI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import com.user_service.DTO.CreateUserRequest;
import com.user_service.DTO.UpdateRoleRequest;
import com.user_service.DTO.UpdateUserRequest;
import com.user_service.DTO.UserResponseDTO;
import com.user_service.Entity.User;
import com.user_service.Service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{email:.+}")
    public UserResponseDTO getUser(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return toDTO(user);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(request.getEmail(), request.getName(), request.getRole());
        return ResponseEntity
                .created(URI.create("/users/" + user.getEmail()))
                .body(toDTO(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PutMapping("/{email:.+}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String email, @RequestBody @Valid UpdateUserRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!email.equals(currentUserEmail)) {
            throw new AccessDeniedException("You can only update your own profile.");
        }
        User user = userService.updateUser(email, request.getName(), request.getBio(), request.getExperience(), request.getSkills(), request.getPortfolioLinks());
        return ResponseEntity.ok(toDTO(user));
    }

    @PutMapping("/{email:.+}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable String email, @RequestBody @Valid UpdateRoleRequest request) {
        User user = userService.updateUserRole(email, request.getRole());
        return ResponseEntity.ok(toDTO(user));
    }

    private UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setBio(user.getBio());
        dto.setExperience(user.getExperience());
        dto.setSkills(user.getSkills());
        dto.setPortfolioLinks(user.getPortfolioLinks());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
