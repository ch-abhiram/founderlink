package com.messaging_service.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationResponseDTO {
    private Long id;
    private Long startupId;
    private String startupName;
    private String participantEmail;
    private String founderEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
