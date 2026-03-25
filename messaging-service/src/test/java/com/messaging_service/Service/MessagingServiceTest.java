package com.messaging_service.Service;

import com.messaging_service.DTO.MessageResponseDTO;
import com.messaging_service.DTO.SendMessageRequest;
import com.messaging_service.DTO.StartupDto;
import com.messaging_service.Entity.Conversation;
import com.messaging_service.Entity.Message;
import com.messaging_service.Feign.StartupClient;
import com.messaging_service.Repository.ConversationRepository;
import com.messaging_service.Repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private StartupClient startupClient;

    @InjectMocks
    private MessagingService messagingService;

    private SendMessageRequest request;
    private StartupDto startupDto;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        request = new SendMessageRequest();
        request.setStartupId(1L);
        request.setContent("Hello world");

        startupDto = new StartupDto();
        startupDto.setId(1L);
        startupDto.setName("Test Startup");
        startupDto.setFounderEmail("founder@test.com");

        conversation = new Conversation();
        conversation.setId(10L);
        conversation.setStartupId(1L);
        conversation.setParticipantEmail("user@test.com");
    }

    private void setupSecurityContext(String email) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testSendMessage_AsParticipant() {
        setupSecurityContext("user@test.com");
        
        when(startupClient.getStartup(1L)).thenReturn(startupDto);
        when(conversationRepository.findByStartupIdAndParticipantEmail(1L, "user@test.com"))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> {
            Message m = i.getArgument(0);
            m.setId(100L);
            return m;
        });

        MessageResponseDTO result = messagingService.sendMessage(request);

        assertNotNull(result);
        assertEquals(10L, result.getConversationId());
        assertEquals("user@test.com", result.getSenderEmail());
        assertEquals("Hello world", result.getContent());
    }

    @Test
    void testSendMessage_AsFounder_RequiresParticipantEmail() {
        setupSecurityContext("founder@test.com");
        
        when(startupClient.getStartup(1L)).thenReturn(startupDto);
        // request.setParticipantEmail is null here, so it should fail

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            messagingService.sendMessage(request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(messageRepository, never()).save(any());
    }

    @Test
    void testSendMessage_AsFounder_Success() {
        setupSecurityContext("founder@test.com");
        request.setParticipantEmail("user@test.com");
        
        when(startupClient.getStartup(1L)).thenReturn(startupDto);
        when(conversationRepository.findByStartupIdAndParticipantEmail(1L, "user@test.com"))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> {
            Message m = i.getArgument(0);
            m.setId(100L);
            return m;
        });

        MessageResponseDTO result = messagingService.sendMessage(request);

        assertNotNull(result);
        assertEquals(10L, result.getConversationId());
        assertEquals("founder@test.com", result.getSenderEmail());
    }
}
