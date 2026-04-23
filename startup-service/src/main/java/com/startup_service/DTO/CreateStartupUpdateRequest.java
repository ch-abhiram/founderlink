package com.startup_service.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStartupUpdateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
