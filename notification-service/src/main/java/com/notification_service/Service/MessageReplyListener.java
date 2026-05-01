package com.notification_service.Service;

import com.notification_service.Config.RabbitConfig;
import com.notification_service.DTO.MessageReplyEvent;
import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageReplyListener {

    private final EmailService emailService;
    private final NotificationRepository repository;

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

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFY_MESSAGE_RECEIVED)
    public void handleMessageReceived(MessageReplyEvent event) {
        if (event.getRecipientEmail() == null || event.getRecipientEmail().isBlank()) {
            log.warn("Skipping message notification because recipientEmail is missing for conversationId={}", event.getConversationId());
            return;
        }

        Notification notification = new Notification();
        notification.setUserEmail(event.getRecipientEmail());
        notification.setType("MESSAGE");
        notification.setTitle("New Message");
        notification.setMessage(
                (event.getSenderEmail() == null ? "Someone" : event.getSenderEmail()) +
                " sent a message about '" + event.getStartupName() + "'."
        );
        repository.save(notification);
    }
}
