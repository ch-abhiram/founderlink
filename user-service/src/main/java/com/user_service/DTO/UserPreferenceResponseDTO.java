package com.user_service.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class UserPreferenceResponseDTO {
    private Long id;
    private String userEmail;
    private List<String> industries;
    private List<String> stages;
    private String fundingRange;
    private String collabStyle;
    private String linkedinUrl;
    private LocalDateTime updatedAt;
}
