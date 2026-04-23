package com.startup_service.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StartupDocumentResponseDTO {
    private Long id;
    private Long startupId;
    private String name;
    private String url;
    private String docType;
    private LocalDateTime createdAt;
}
