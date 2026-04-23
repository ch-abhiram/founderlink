package com.startup_service.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StartupUpdateResponseDTO {
    private Long id;
    private Long startupId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
