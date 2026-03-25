package com.messaging_service.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponseDTO {
    private Long id;
    private Long conversationId;
    private String senderEmail;
    private String content;
    private LocalDateTime createdAt;
}
