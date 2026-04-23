package com.user_service.DTO;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String bio;
    private String experience;
    private String headline;
    private String location;
    private String avatarUrl;
    private String primaryGoal;
    private java.util.List<String> skills;
    private java.util.List<String> portfolioLinks;
    private java.time.LocalDateTime createdAt;
}
