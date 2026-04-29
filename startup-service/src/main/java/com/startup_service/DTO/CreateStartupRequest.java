package com.startup_service.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateStartupRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    @Positive
    private Double fundingGoal;
    @NotBlank
    private String category;
    private String tagline;
    private String location;
    private Integer foundedYear;
    private Integer teamSize;
    private Double mrr;
    private String stage;
    private String currentRound;
    private Double valuation;
    private Double equityOffered;
    private String websiteUrl;
    private String logoUrl;
    private String linkedinUrl;
    private String twitterUrl;
}
