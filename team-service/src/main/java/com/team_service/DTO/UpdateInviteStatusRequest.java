package com.team_service.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateInviteStatusRequest {
    
    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "(?i)PENDING|ACCEPTED|REJECTED", message = "Status must be one of PENDING, ACCEPTED, or REJECTED")
    private String status; // ACCEPTED, REJECTED
}
