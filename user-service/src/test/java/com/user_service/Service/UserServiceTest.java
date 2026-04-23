package com.user_service.Service;

import com.user_service.Entity.User;
import com.user_service.Exception.ConflictException;
import com.user_service.Exception.UserNotFoundException;
import com.user_service.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setName("Test User");
        mockUser.setRole("ROLE_FOUNDER");
    }

    @Test
    void testGetUserByEmail_Found() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        
        User user = userService.getUserByEmail("test@test.com");
        
        assertNotNull(user);
        assertEquals("test@test.com", user.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByEmail("notfound@test.com");
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testCreateUser_HappyPath() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User newUser = userService.createUser("new@test.com", "New User", "ROLE_INVESTOR");

        assertNotNull(newUser);
        assertEquals("new@test.com", newUser.getEmail());
        assertEquals("ROLE_INVESTOR", newUser.getRole());
    }

    @Test
    void testCreateUser_DuplicateEmail() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        Exception exception = assertThrows(ConflictException.class, () -> {
            userService.createUser("test@test.com", "Another Name", "ROLE_FOUNDER");
        });

        assertTrue(exception.getMessage().contains("User already exists"));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.updateUser(
                "test@test.com",
                "Updated",
                "Bio",
                "5 years",
                "Founder building tools",
                "Bengaluru",
                "https://example.com/avatar.png",
                "Find a cofounder",
                List.of("Java"),
                List.of("portfolio"));

        assertEquals("Updated", updated.getName());
        assertEquals("Bio", updated.getBio());
        assertEquals("Founder building tools", updated.getHeadline());
        assertEquals("Bengaluru", updated.getLocation());
        assertEquals(List.of("Java"), updated.getSkills());
    }

    @Test
    void testUpdateUserRole() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.updateUserRole("test@test.com", "ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", updated.getRole());
    }
}
