package com.startup_service.DTO;

import lombok.Data;

@Data
public class UpdateStartupRequest {
    private String name;
    private String description;
    private Double fundingGoal;
    private String category;
    private String tagline;
    private String location;
    private Integer foundedYear;
    private Integer teamSize;
    private Double mrr;
    private String stage;
    private String currentRound;
    private Double valuation;
}
