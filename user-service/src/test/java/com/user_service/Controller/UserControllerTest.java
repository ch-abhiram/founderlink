package com.user_service.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.user_service.DTO.CreateUserRequest;
import com.user_service.DTO.UpdateRoleRequest;
import com.user_service.DTO.UpdateUserPreferenceRequest;
import com.user_service.DTO.UpdateUserRequest;
import com.user_service.Entity.User;
import com.user_service.Entity.UserPreference;
import com.user_service.Service.UserPreferenceService;
import com.user_service.Service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserPreferenceService preferenceService;

    private UserController controller;
    private User user;
    private UserPreference preference;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, preferenceService);
        user = user();
        preference = preference();
        authenticate("test@test.com", "ROLE_FOUNDER");
    }

    @Test
    void getAndCreateUserMapEntitiesToDtos() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@test.com");
        request.setName("Test User");
        request.setRole("ROLE_FOUNDER");
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        when(userService.createUser("test@test.com", "Test User", "ROLE_FOUNDER")).thenReturn(user);

        assertEquals("Test User", controller.getUser("test@test.com").getName());
        var response = controller.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/users/test@test.com", response.getHeaders().getLocation().toString());
        assertEquals(List.of("Java", "Angular"), response.getBody().getSkills());
    }

    @Test
    void adminEndpointsMapListsAndRoleUpdates() {
        authenticate("admin@test.com", "ROLE_ADMIN");
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("ROLE_ADMIN");
        user.setRole("ROLE_ADMIN");
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userService.updateUserRole("test@test.com", "ROLE_ADMIN")).thenReturn(user);

        assertEquals(1, controller.getAllUsers().getBody().size());
        assertEquals("ROLE_ADMIN", controller.updateUserRole("test@test.com", request).getBody().getRole());
    }

    @Test
    void updateUserAllowsSelfAndMapsProfileFields() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated");
        request.setBio("Bio");
        request.setExperience("5 years");
        request.setHeadline("Founder");
        request.setLocation("Bengaluru");
        request.setAvatarUrl("avatar.png");
        request.setPrimaryGoal("Raise seed");
        request.setSkills(List.of("Java"));
        request.setPortfolioLinks(List.of("https://example.com"));
        user.setName("Updated");
        when(userService.updateUser(eq("test@test.com"), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(user);

        var response = controller.updateUser("test@test.com", request);

        assertEquals("Updated", response.getBody().getName());
        verify(userService).updateUser(
                "test@test.com",
                "Updated",
                "Bio",
                "5 years",
                "Founder",
                "Bengaluru",
                "avatar.png",
                "Raise seed",
                List.of("Java"),
                List.of("https://example.com"));
    }

    @Test
    void updateUserRejectsOtherUser() {
        assertThrows(AccessDeniedException.class,
                () -> controller.updateUser("other@test.com", new UpdateUserRequest()));
    }

    @Test
    void preferencesAllowSelfOrAdminAndMapDto() {
        UpdateUserPreferenceRequest request = new UpdateUserPreferenceRequest();
        request.setIndustries(List.of("Fintech"));
        when(preferenceService.getPreferencesByEmail("test@test.com")).thenReturn(preference);
        when(preferenceService.updatePreferences("test@test.com", request)).thenReturn(preference);

        assertEquals("Seed", controller.getUserPreferences("test@test.com").getBody().getStages().get(0));
        assertEquals("remote", controller.updateUserPreferences("test@test.com", request).getBody().getCollabStyle());

        authenticate("admin@test.com", "ROLE_ADMIN");
        when(preferenceService.getPreferencesByEmail("test@test.com")).thenReturn(preference);
        assertEquals("test@test.com", controller.getUserPreferences("test@test.com").getBody().getUserEmail());
    }

    @Test
    void preferencesRejectOtherNonAdminUser() {
        assertThrows(AccessDeniedException.class,
                () -> controller.getUserPreferences("other@test.com"));
    }

    private void authenticate(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(role))));
    }

    private User user() {
        User value = new User();
        value.setId(1L);
        value.setEmail("test@test.com");
        value.setName("Test User");
        value.setRole("ROLE_FOUNDER");
        value.setBio("Bio");
        value.setExperience("3 years");
        value.setHeadline("Founder");
        value.setLocation("Bengaluru");
        value.setAvatarUrl("avatar.png");
        value.setPrimaryGoal("Find investors");
        value.setSkills(List.of("Java", "Angular"));
        value.setPortfolioLinks(List.of("https://example.com"));
        value.setCreatedAt(LocalDateTime.now());
        return value;
    }

    private UserPreference preference() {
        UserPreference value = new UserPreference();
        value.setId(2L);
        value.setUserEmail("test@test.com");
        value.setIndustries(List.of("Fintech"));
        value.setStages(List.of("Seed"));
        value.setFundingRange("100k-500k");
        value.setCollabStyle("remote");
        value.setLinkedinUrl("https://linkedin.com/in/test");
        value.setUpdatedAt(LocalDateTime.now());
        return value;
    }
}
