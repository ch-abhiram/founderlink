package com.messaging_service.Service;

import com.messaging_service.DTO.ConversationResponseDTO;
import com.messaging_service.DTO.MessageResponseDTO;
import com.messaging_service.DTO.SendMessageRequest;
import com.messaging_service.DTO.StartupDto;
import com.messaging_service.Entity.Conversation;
import com.messaging_service.Entity.Message;
import com.messaging_service.Feign.StartupClient;
import com.messaging_service.Repository.ConversationRepository;
import com.messaging_service.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final StartupClient startupClient;

    @Transactional
    public MessageResponseDTO sendMessage(SendMessageRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        StartupDto startup;
        try {
            startup = startupClient.getStartup(request.getStartupId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        }

        String participantEmail;
        
        // If the current user is the startup founder, they must specify who they are replying to
        if (startup.getFounderEmail().equals(currentUser)) {
            if (request.getParticipantEmail() == null || request.getParticipantEmail().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Founder must specify participant email to reply to");
            }
            participantEmail = request.getParticipantEmail();
        } else {
            // Otherwise, the current user is the participant initiating or contributing to the chat
            participantEmail = currentUser;
        }

        // Find or create conversation
        Conversation conversation = conversationRepository.findByStartupIdAndParticipantEmail(request.getStartupId(), participantEmail)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setStartupId(request.getStartupId());
                    newConv.setParticipantEmail(participantEmail);
                    return conversationRepository.save(newConv);
                });

        // Update conversation's timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Save message
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderEmail(currentUser);
        message.setContent(request.getContent());
        Message savedMessage = messageRepository.save(message);

        return toMessageDto(savedMessage);
    }

    public List<MessageResponseDTO> getMessages(Long conversationId) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
                
        verifyAccess(conversation, currentUser);

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toMessageDto)
                .collect(Collectors.toList());
    }

    public List<ConversationResponseDTO> getStartupConversations(Long startupId) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        StartupDto startup;
        try {
            startup = startupClient.getStartup(startupId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        }

        if (!startup.getFounderEmail().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the founder can view all startup conversations");
        }

        return conversationRepository.findByStartupId(startupId).stream()
                .map(this::toConversationDto)
                .collect(Collectors.toList());
    }

    public List<ConversationResponseDTO> getMyConversations() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return conversationRepository.findByParticipantEmail(currentUser).stream()
                .map(this::toConversationDto)
                .collect(Collectors.toList());
    }

    private void verifyAccess(Conversation conversation, String currentUser) {
        if (conversation.getParticipantEmail().equals(currentUser)) {
            return;
        }
        
        StartupDto startup;
        try {
            startup = startupClient.getStartup(conversation.getStartupId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error resolving startup");
        }
        
        if (!startup.getFounderEmail().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this conversation");
        }
    }

    private MessageResponseDTO toMessageDto(Message message) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderEmail(message.getSenderEmail());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    private ConversationResponseDTO toConversationDto(Conversation conversation) {
        ConversationResponseDTO dto = new ConversationResponseDTO();
        dto.setId(conversation.getId());
        dto.setStartupId(conversation.getStartupId());
        dto.setParticipantEmail(conversation.getParticipantEmail());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        return dto;
    }
}
