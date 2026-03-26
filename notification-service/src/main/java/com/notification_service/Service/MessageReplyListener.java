package com.notification_service.Service;

import com.notification_service.Config.RabbitConfig;
import com.notification_service.DTO.MessageReplyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageReplyListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFY_MESSAGE_REPLY)
    public void handleFounderReply(MessageReplyEvent event) {
        try {
            emailService.sendFounderReplyEmail(
                    event.getRecipientEmail(),
                    event.getStartupName(),
                    event.getFounderEmail(),
                    event.getContent()
            );
        } catch (Exception ex) {
            // Keep listener resilient; do not break queue consumption for transient SMTP issues.
            log.warn("Failed to send founder reply email to recipient={}: {}", event.getRecipientEmail(), ex.getMessage());
        }
    }
}
