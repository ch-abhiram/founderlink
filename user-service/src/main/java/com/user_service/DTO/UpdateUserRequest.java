package com.user_service.DTO;

import lombok.Data;
import java.util.List;

@Data
public class UpdateUserRequest {
    private String name;
    private String bio;
    private String experience;
    private String headline;
    private String location;
    private String avatarUrl;
    private String primaryGoal;
    private List<String> skills;
    private List<String> portfolioLinks;
}
