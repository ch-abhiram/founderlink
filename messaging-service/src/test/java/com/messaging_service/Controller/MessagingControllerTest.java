package com.messaging_service.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.messaging_service.DTO.ConversationResponseDTO;
import com.messaging_service.DTO.MessageResponseDTO;
import com.messaging_service.DTO.SendMessageRequest;
import com.messaging_service.Service.MessagingService;

@ExtendWith(MockitoExtension.class)
class MessagingControllerTest {

    @Mock
    private MessagingService service;

    private MessagingController controller;

    @BeforeEach
    void setUp() {
        controller = new MessagingController(service);
    }

    @Test
    void sendMessageReturnsCreatedMessage() {
        SendMessageRequest request = new SendMessageRequest();
        MessageResponseDTO message = message();
        when(service.sendMessage(request)).thenReturn(message);

        var response = controller.sendMessage(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/messages/conversation/9", response.getHeaders().getLocation().toString());
        assertEquals("Hello", response.getBody().getContent());
    }

    @Test
    void listEndpointsReturnServiceDtos() {
        when(service.getMessages(9L)).thenReturn(List.of(message()));
        when(service.getStartupConversations(7L)).thenReturn(List.of(conversation()));
        when(service.getMyConversations()).thenReturn(List.of(conversation()));

        assertEquals(1, controller.getMessages(9L).getBody().size());
        assertEquals("SignalForge", controller.getStartupConversations(7L).getBody().get(0).getStartupName());
        assertEquals(9L, controller.getMyConversations().getBody().get(0).getId());
    }

    private MessageResponseDTO message() {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setId(3L);
        dto.setConversationId(9L);
        dto.setSenderEmail("founder@test.com");
        dto.setContent("Hello");
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    private ConversationResponseDTO conversation() {
        ConversationResponseDTO dto = new ConversationResponseDTO();
        dto.setId(9L);
        dto.setStartupId(7L);
        dto.setStartupName("SignalForge");
        dto.setFounderEmail("founder@test.com");
        dto.setParticipantEmail("investor@test.com");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}
