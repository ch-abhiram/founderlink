package com.startup_service.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StartupResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String founderEmail;
    private String tagline;
    private String location;
    private Integer foundedYear;
    private Integer teamSize;
    private Double mrr;
    private Double fundingGoal;
    private Double currentFunding;
    private String category;
    private String stage;
    private String currentRound;
    private Double valuation;
    private String status;
    private Double equityOffered;
    private String websiteUrl;
    private String logoUrl;
    private String linkedinUrl;
    private String twitterUrl;
    private int followersCount;
    private LocalDateTime createdAt;
}
