package com.team_service.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateInviteStatusRequest {
    
    @NotBlank(message = "Status cannot be blank")
    private String status; // ACCEPTED, REJECTED
}
