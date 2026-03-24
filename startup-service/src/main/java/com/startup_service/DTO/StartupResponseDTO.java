package com.startup_service.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StartupResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String founderEmail;
    private Double fundingGoal;
    private Double currentFunding;
    private String category;
    private String currentRound;
    private Double valuation;
    private String status;
    private int followersCount;
    private LocalDateTime createdAt;
}
