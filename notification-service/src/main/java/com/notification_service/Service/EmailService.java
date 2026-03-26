package com.notification_service.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${founderlink.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${founderlink.mail.from:no-reply@founderlink.local}")
    private String fromEmail;

    public void sendFounderReplyEmail(String recipientEmail, String startupName, String founderEmail, String content) {
        if (!mailEnabled) {
            log.info("Mail disabled. Skipping founder reply email to recipient={}", recipientEmail);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail enabled but JavaMailSender is not configured. Skipping email to recipient={}", recipientEmail);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("New message from startup founder: " + startupName);
        message.setText(buildFounderReplyBody(startupName, founderEmail, content));
        mailSender.send(message);
    }

    private String buildFounderReplyBody(String startupName, String founderEmail, String content) {
        return "You received a new message from " + founderEmail + " (" + startupName + ").\n\n"
                + "Message:\n"
                + content + "\n\n"
                + "Open FounderLink to continue the conversation.";
    }
}
