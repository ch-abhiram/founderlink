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
import java.util.List;
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

    @Test
    void testGetMessagesReturnsConversationMessages() {
        setupSecurityContext("user@test.com");
        Message message = new Message();
        message.setId(100L);
        message.setConversationId(10L);
        message.setSenderEmail("user@test.com");
        message.setContent("Hello world");

        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(message));

        List<MessageResponseDTO> result = messagingService.getMessages(10L);

        assertEquals(1, result.size());
        assertEquals("Hello world", result.get(0).getContent());
    }

    @Test
    void testGetStartupConversationsFounderOnly() {
        setupSecurityContext("founder@test.com");
        when(startupClient.getStartup(1L)).thenReturn(startupDto);
        when(conversationRepository.findByStartupId(1L)).thenReturn(List.of(conversation));

        var result = messagingService.getStartupConversations(1L);

        assertEquals(1, result.size());
        assertEquals("user@test.com", result.get(0).getParticipantEmail());
    }

    @Test
    void testGetMyConversationsMergesParticipantAndSenderViews() {
        setupSecurityContext("user@test.com");
        Message sentMessage = new Message();
        sentMessage.setConversationId(10L);
        when(conversationRepository.findByParticipantEmail("user@test.com")).thenReturn(List.of(conversation));
        when(messageRepository.findBySenderEmailOrderByCreatedAtDesc("user@test.com")).thenReturn(List.of(sentMessage));
        when(conversationRepository.findAllById(any())).thenReturn(List.of(conversation));

        var result = messagingService.getMyConversations();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }
}
