package com.team_service.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamMemberResponseDTO {
    private Long id;
    private Long startupId;
    private String userEmail;
    private String role;
    private String status;
    private Double equityPercentage;
    private String permissionLevel;
    private LocalDateTime createdAt;
}
