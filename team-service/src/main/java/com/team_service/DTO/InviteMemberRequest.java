package com.team_service.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InviteMemberRequest {

    @NotNull(message = "Startup ID is required")
    private Long startupId;

    @NotBlank(message = "User email is required")
    private String userEmail;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "COFOUNDER|EMPLOYEE|ADVISOR|INTERN", message = "Invalid team role")
    private String role;
}
