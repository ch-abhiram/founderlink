package com.auth_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth_service.DTO.CreateUserRequest;
import com.auth_service.DTO.UserResponse;
import com.auth_service.Feign.UserClient;

import feign.FeignException;

class UserServiceClientWrapperTest {

    private UserClient userClient;
    private UserServiceClientWrapper wrapper;

    @BeforeEach
    void setUp() {
        userClient = mock(UserClient.class);
        wrapper = new UserServiceClientWrapper(userClient);
    }

    @Test
    void getUserRoleReturnsRoleFromUserService() {
        UserResponse response = new UserResponse();
        response.setRole("ROLE_ADMIN");
        when(userClient.getUser("admin@test.com")).thenReturn(response);

        assertEquals("ROLE_ADMIN", wrapper.getUserRole("admin@test.com"));
    }

    @Test
    void createUserProfileSkipsExistingProfile() {
        when(userClient.getUser("existing@test.com")).thenReturn(new UserResponse());

        wrapper.createUserProfile("existing@test.com", "ROLE_FOUNDER", "Existing", "User");

        verify(userClient, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUserProfileCreatesMissingProfileWithCombinedName() {
        when(userClient.getUser("new@test.com")).thenThrow(mock(FeignException.NotFound.class));

        wrapper.createUserProfile("new@test.com", "ROLE_INVESTOR", "New", "User");

        verify(userClient).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUserProfileDerivesNameWhenNamesAreBlank() {
        when(userClient.getUser("derived@test.com")).thenThrow(mock(FeignException.NotFound.class));

        wrapper.createUserProfile("derived@test.com", "ROLE_INVESTOR", " ", null);

        verify(userClient).createUser(any(CreateUserRequest.class));
    }

    @Test
    void fallbackReturnsGenericUserRole() {
        assertEquals("USER", wrapper.fallback("user@test.com", new RuntimeException("down")));
    }
}
