package com.auth_service.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @jakarta.validation.constraints.NotNull(message = "Role is required")
    @jakarta.validation.constraints.Pattern(regexp="ROLE_FOUNDER|ROLE_INVESTOR|ROLE_COFOUNDER|ROLE_ADMIN", message="Invalid role")
    private String role;
}
