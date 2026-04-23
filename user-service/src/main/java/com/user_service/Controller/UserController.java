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
import com.user_service.DTO.UpdateUserPreferenceRequest;
import com.user_service.DTO.UpdateRoleRequest;
import com.user_service.DTO.UpdateUserRequest;
import com.user_service.DTO.UserPreferenceResponseDTO;
import com.user_service.DTO.UserResponseDTO;
import com.user_service.Entity.User;
import com.user_service.Entity.UserPreference;
import com.user_service.Service.UserPreferenceService;
import com.user_service.Service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserPreferenceService userPreferenceService;

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
        ensureSelf(email);
        User user = userService.updateUser(
                email,
                request.getName(),
                request.getBio(),
                request.getExperience(),
                request.getHeadline(),
                request.getLocation(),
                request.getAvatarUrl(),
                request.getPrimaryGoal(),
                request.getSkills(),
                request.getPortfolioLinks());
        return ResponseEntity.ok(toDTO(user));
    }

    @GetMapping("/{email:.+}/preferences")
    public ResponseEntity<UserPreferenceResponseDTO> getUserPreferences(@PathVariable String email) {
        ensureSelfOrAdmin(email);
        return ResponseEntity.ok(toPreferenceDTO(userPreferenceService.getPreferencesByEmail(email)));
    }

    @PutMapping("/{email:.+}/preferences")
    public ResponseEntity<UserPreferenceResponseDTO> updateUserPreferences(
            @PathVariable String email,
            @RequestBody @Valid UpdateUserPreferenceRequest request) {
        ensureSelfOrAdmin(email);
        UserPreference preference = userPreferenceService.updatePreferences(email, request);
        return ResponseEntity.ok(toPreferenceDTO(preference));
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
        dto.setHeadline(user.getHeadline());
        dto.setLocation(user.getLocation());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPrimaryGoal(user.getPrimaryGoal());
        dto.setSkills(user.getSkills());
        dto.setPortfolioLinks(user.getPortfolioLinks());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private UserPreferenceResponseDTO toPreferenceDTO(UserPreference preference) {
        UserPreferenceResponseDTO dto = new UserPreferenceResponseDTO();
        dto.setId(preference.getId());
        dto.setUserEmail(preference.getUserEmail());
        dto.setIndustries(preference.getIndustries());
        dto.setStages(preference.getStages());
        dto.setFundingRange(preference.getFundingRange());
        dto.setCollabStyle(preference.getCollabStyle());
        dto.setLinkedinUrl(preference.getLinkedinUrl());
        dto.setUpdatedAt(preference.getUpdatedAt());
        return dto;
    }

    private void ensureSelf(String email) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!email.equals(currentUserEmail)) {
            throw new AccessDeniedException("You can only update your own profile.");
        }
    }

    private void ensureSelfOrAdmin(String email) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!email.equals(currentUserEmail) && !isAdmin) {
            throw new AccessDeniedException("You can only access your own preferences.");
        }
    }
}
