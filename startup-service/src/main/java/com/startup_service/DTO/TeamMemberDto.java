package com.startup_service.DTO;

import lombok.Data;

@Data
public class TeamMemberDto {
    private Long id;
    private Long startupId;
    private String userEmail;
    private String role;
    private String status;
    private Double equityPercentage;
    private String permissionLevel;
}
