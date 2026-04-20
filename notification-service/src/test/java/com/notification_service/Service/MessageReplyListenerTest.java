package com.notification_service.Service;

import com.notification_service.DTO.MessageReplyEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageReplyListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MessageReplyListener listener;

    @Test
    void testHandleFounderReplyDelegatesToEmailService() {
        MessageReplyEvent event = new MessageReplyEvent();
        event.setRecipientEmail("user@test.com");
        event.setStartupName("MyStartup");
        event.setFounderEmail("founder@test.com");
        event.setContent("Hello there");

        listener.handleFounderReply(event);

        verify(emailService).sendFounderReplyEmail("user@test.com", "MyStartup", "founder@test.com", "Hello there");
    }

    @Test
    void testHandleFounderReplySwallowsEmailFailures() {
        MessageReplyEvent event = new MessageReplyEvent();
        event.setRecipientEmail("user@test.com");
        event.setStartupName("MyStartup");
        event.setFounderEmail("founder@test.com");
        event.setContent("Hello there");

        doThrow(new RuntimeException("smtp down"))
                .when(emailService)
                .sendFounderReplyEmail("user@test.com", "MyStartup", "founder@test.com", "Hello there");

        listener.handleFounderReply(event);

        verify(emailService).sendFounderReplyEmail("user@test.com", "MyStartup", "founder@test.com", "Hello there");
    }
}
