package com.user_service.Service;


import org.springframework.stereotype.Service;

import com.user_service.Entity.User;
import com.user_service.Exception.UserNotFoundException;
import com.user_service.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    public User createUser(String email, String name, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists: " + email);
        }
        User user = new User();
        user.setEmail(email);
        user.setName(name != null ? name : "");
        user.setRole(role != null ? role : "USER");
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(String email, String name, String bio, String experience, List<String> skills, List<String> portfolioLinks) {
        User user = getUserByEmail(email);
        if (name != null) user.setName(name);
        if (bio != null) user.setBio(bio);
        if (experience != null) user.setExperience(experience);
        if (skills != null) user.setSkills(skills);
        if (portfolioLinks != null) user.setPortfolioLinks(portfolioLinks);
        return userRepository.save(user);
    }

    public User updateUserRole(String email, String newRole) {
        User user = getUserByEmail(email);
        user.setRole(newRole);
        return userRepository.save(user);
    }
}
