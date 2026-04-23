package com.user_service.Service;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.user_service.DTO.UpdateUserPreferenceRequest;
import com.user_service.Entity.UserPreference;
import com.user_service.Exception.UserNotFoundException;
import com.user_service.Repository.UserPreferenceRepository;
import com.user_service.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreference getPreferencesByEmail(String email) {
        ensureUserExists(email);
        return userPreferenceRepository.findByUserEmail(email)
                .orElseGet(() -> createEmptyPreference(email));
    }

    public UserPreference updatePreferences(String email, UpdateUserPreferenceRequest request) {
        ensureUserExists(email);
        UserPreference preference = userPreferenceRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    UserPreference created = new UserPreference();
                    created.setUserEmail(email);
                    return created;
                });

        if (request.getIndustries() != null) {
            preference.setIndustries(request.getIndustries());
        }
        if (request.getStages() != null) {
            preference.setStages(request.getStages());
        }
        if (request.getFundingRange() != null) {
            preference.setFundingRange(request.getFundingRange());
        }
        if (request.getCollabStyle() != null) {
            preference.setCollabStyle(request.getCollabStyle());
        }
        if (request.getLinkedinUrl() != null) {
            preference.setLinkedinUrl(request.getLinkedinUrl());
        }

        return userPreferenceRepository.save(preference);
    }

    private void ensureUserExists(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    private UserPreference createEmptyPreference(String email) {
        UserPreference preference = new UserPreference();
        preference.setUserEmail(email);
        preference.setIndustries(Collections.emptyList());
        preference.setStages(Collections.emptyList());
        return preference;
    }
}
