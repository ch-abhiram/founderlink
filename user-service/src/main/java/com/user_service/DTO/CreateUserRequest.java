package com.user_service.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    @NotNull
    @Pattern(regexp = "ROLE_FOUNDER|ROLE_INVESTOR|ROLE_COFOUNDER")
    private String role;
}
