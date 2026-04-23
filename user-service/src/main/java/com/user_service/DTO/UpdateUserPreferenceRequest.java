package com.user_service.DTO;

import java.util.List;

import lombok.Data;

@Data
public class UpdateUserPreferenceRequest {
    private List<String> industries;
    private List<String> stages;
    private String fundingRange;
    private String collabStyle;
    private String linkedinUrl;
}
