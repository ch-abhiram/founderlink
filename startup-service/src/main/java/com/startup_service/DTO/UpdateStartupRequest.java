package com.startup_service.DTO;

import lombok.Data;

@Data
public class UpdateStartupRequest {
    private String name;
    private String description;
    private Double fundingGoal;
    private String category;
    private String currentRound;
    private Double valuation;
}
