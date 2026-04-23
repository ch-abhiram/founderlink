package com.user_service.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotBlank
    @Pattern(regexp = "ROLE_FOUNDER|ROLE_INVESTOR|ROLE_COFOUNDER|ROLE_ADMIN")
    private String role;
}
