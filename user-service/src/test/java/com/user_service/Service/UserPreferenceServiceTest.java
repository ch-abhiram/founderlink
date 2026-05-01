package com.user_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.user_service.DTO.UpdateUserPreferenceRequest;
import com.user_service.Entity.User;
import com.user_service.Entity.UserPreference;
import com.user_service.Exception.UserNotFoundException;
import com.user_service.Repository.UserPreferenceRepository;
import com.user_service.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceRepository preferenceRepository;

    @InjectMocks
    private UserPreferenceService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@test.com");
    }

    @Test
    void getPreferencesReturnsExistingPreference() {
        UserPreference preference = new UserPreference();
        preference.setUserEmail("test@test.com");
        preference.setIndustries(List.of("SaaS"));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(preference));

        assertEquals(List.of("SaaS"), service.getPreferencesByEmail("test@test.com").getIndustries());
    }

    @Test
    void getPreferencesCreatesEmptyPreferenceWhenMissing() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserEmail("test@test.com")).thenReturn(Optional.empty());

        UserPreference preference = service.getPreferencesByEmail("test@test.com");

        assertEquals("test@test.com", preference.getUserEmail());
        assertEquals(List.of(), preference.getIndustries());
        assertEquals(List.of(), preference.getStages());
    }

    @Test
    void updatePreferencesCreatesAndAppliesOnlyProvidedFields() {
        UpdateUserPreferenceRequest request = new UpdateUserPreferenceRequest();
        request.setIndustries(List.of("Fintech"));
        request.setStages(List.of("Seed"));
        request.setFundingRange("100k-500k");
        request.setCollabStyle("hybrid");
        request.setLinkedinUrl("https://linkedin.com/in/test");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserEmail("test@test.com")).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(UserPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreference updated = service.updatePreferences("test@test.com", request);

        assertEquals(List.of("Fintech"), updated.getIndustries());
        assertEquals(List.of("Seed"), updated.getStages());
        assertEquals("100k-500k", updated.getFundingRange());
        assertEquals("hybrid", updated.getCollabStyle());
        assertEquals("https://linkedin.com/in/test", updated.getLinkedinUrl());
    }

    @Test
    void updatePreferencesKeepsExistingValuesWhenRequestFieldsAreNull() {
        UserPreference existing = new UserPreference();
        existing.setUserEmail("test@test.com");
        existing.setIndustries(List.of("AI"));
        existing.setStages(List.of("MVP"));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(existing));
        when(preferenceRepository.save(existing)).thenReturn(existing);

        UserPreference updated = service.updatePreferences("test@test.com", new UpdateUserPreferenceRequest());

        assertEquals(List.of("AI"), updated.getIndustries());
        assertEquals(List.of("MVP"), updated.getStages());
    }

    @Test
    void missingUserThrows() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.getPreferencesByEmail("missing@test.com"));
    }
}
