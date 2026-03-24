package com.user_service.DTO;

import lombok.Data;
import java.util.List;

@Data
public class UpdateUserRequest {
    private String name;
    private String bio;
    private String experience;
    private List<String> skills;
    private List<String> portfolioLinks;
}
