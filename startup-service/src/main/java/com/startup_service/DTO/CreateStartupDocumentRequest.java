package com.startup_service.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStartupDocumentRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String url;

    private String docType;
}
