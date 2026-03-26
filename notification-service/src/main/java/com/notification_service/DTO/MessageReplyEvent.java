package com.notification_service.DTO;

import lombok.Data;

@Data
public class MessageReplyEvent {
    private Long startupId;
    private String startupName;
    private String founderEmail;
    private String recipientEmail;
    private String content;
    private Long conversationId;
}
