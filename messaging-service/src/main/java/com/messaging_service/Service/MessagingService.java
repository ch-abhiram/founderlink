package com.messaging_service.Service;

import com.messaging_service.Config.RabbitConfig;
import com.messaging_service.DTO.ConversationResponseDTO;
import com.messaging_service.DTO.MessageResponseDTO;
import com.messaging_service.DTO.SendMessageRequest;
import com.messaging_service.DTO.StartupDto;
import com.messaging_service.DTO.TeamMemberDto;
import com.messaging_service.Entity.Conversation;
import com.messaging_service.Entity.Message;
import com.messaging_service.Feign.StartupClient;
import com.messaging_service.Feign.TeamClient;
import com.messaging_service.Repository.ConversationRepository;
import com.messaging_service.Repository.MessageRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final StartupClient startupClient;
    private final TeamClient teamClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public MessageResponseDTO sendMessage(SendMessageRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        StartupDto startup;
        try {
            startup = startupClient.getStartup(request.getStartupId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }

        String participantEmail;
        boolean startupSideMessage = canRepresentStartup(startup, currentUser);
        if (startupSideMessage) {
            if (!hasText(request.getParticipantEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participant email is required to reply from the startup team");
            }
            participantEmail = request.getParticipantEmail().trim();
        } else {
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

        if (startupSideMessage && !participantEmail.equals(currentUser)) {
            publishMessageEvent(startup, participantEmail, savedMessage);
        } else {
            publishMessageEvent(startup, startup.getFounderEmail(), savedMessage);
            getAcceptedTeamMembersForNotification(startup.getId(), currentUser).stream()
                    .map(TeamMemberDto::getUserEmail)
                    .filter(email -> !email.equals(currentUser))
                    .forEach(email -> publishMessageEvent(startup, email, savedMessage));
        }

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
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }

        if (!canRepresentStartup(startup, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the founder or accepted startup team members can view startup conversations");
        }

        return conversationRepository.findByStartupId(startupId).stream()
                .map(this::toConversationDto)
                .collect(Collectors.toList());
    }

    public List<ConversationResponseDTO> getMyConversations() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Conversation> byParticipant = conversationRepository.findByParticipantEmail(currentUser);
        Set<Long> sentConversationIds = messageRepository.findBySenderEmailOrderByCreatedAtDesc(currentUser).stream()
                .map(Message::getConversationId)
                .collect(Collectors.toSet());

        List<Conversation> bySender = sentConversationIds.isEmpty()
                ? List.of()
                : conversationRepository.findAllById(sentConversationIds);
        List<Conversation> byFounder = conversationRepository.findAll().stream()
                .filter(conversation -> isFounderConversation(conversation, currentUser))
                .toList();
        List<Conversation> byTeam = conversationRepository.findAll().stream()
                .filter(conversation -> isAcceptedTeamConversation(conversation, currentUser))
                .toList();

        Map<Long, Conversation> merged = new LinkedHashMap<>();
        byParticipant.forEach(c -> merged.put(c.getId(), c));
        bySender.forEach(c -> merged.put(c.getId(), c));
        byFounder.forEach(c -> merged.put(c.getId(), c));
        byTeam.forEach(c -> merged.put(c.getId(), c));

        return merged.values().stream()
                .sorted(Comparator.comparing(Conversation::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
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
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }
        
        if (!canRepresentStartup(startup, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this conversation");
        }
    }

    private boolean isFounderConversation(Conversation conversation, String currentUser) {
        try {
            StartupDto startup = startupClient.getStartup(conversation.getStartupId());
            return currentUser.equals(startup.getFounderEmail());
        } catch (FeignException.NotFound e) {
            return false;
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }
    }

    private boolean isAcceptedTeamConversation(Conversation conversation, String currentUser) {
        try {
            StartupDto startup = startupClient.getStartup(conversation.getStartupId());
            if (currentUser.equals(startup.getFounderEmail())) {
                return false;
            }
            return isAcceptedTeamMember(startup.getId(), currentUser);
        } catch (FeignException.NotFound e) {
            return false;
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }
    }

    private boolean canRepresentStartup(StartupDto startup, String currentUser) {
        return startup.getFounderEmail().equals(currentUser) || isAcceptedTeamMember(startup.getId(), currentUser);
    }

    private boolean isAcceptedTeamMember(Long startupId, String currentUser) {
        return getAcceptedTeamMembers(startupId, currentUser).stream()
                .anyMatch(member -> currentUser.equals(member.getUserEmail()));
    }

    private List<TeamMemberDto> getAcceptedTeamMembers(Long startupId, String currentUser) {
        try {
            return teamClient.getStartupTeam(startupId, currentUser, currentRole()).stream()
                    .filter(member -> "ACCEPTED".equalsIgnoreCase(member.getStatus()))
                    .toList();
        } catch (FeignException.NotFound e) {
            return List.of();
        } catch (FeignException.Forbidden e) {
            return List.of();
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup team");
        }
    }

    private List<TeamMemberDto> getAcceptedTeamMembersForNotification(Long startupId, String currentUser) {
        try {
            return getAcceptedTeamMembers(startupId, currentUser);
        } catch (ResponseStatusException ex) {
            log.warn("Could not load startup team for message notifications on startupId={}: {}", startupId, ex.getReason());
            return List.of();
        }
    }

    private String currentRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_USER");
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
        StartupDto startup;
        try {
            startup = startupClient.getStartup(conversation.getStartupId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Startup not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load startup details");
        }

        ConversationResponseDTO dto = new ConversationResponseDTO();
        dto.setId(conversation.getId());
        dto.setStartupId(conversation.getStartupId());
        dto.setStartupName(startup.getName());
        dto.setParticipantEmail(conversation.getParticipantEmail());
        dto.setFounderEmail(startup.getFounderEmail());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        return dto;
    }

    private void publishMessageEvent(StartupDto startup, String recipientEmail, Message savedMessage) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("startupId", startup.getId());
            event.put("startupName", startup.getName());
            event.put("founderEmail", startup.getFounderEmail());
            event.put("senderEmail", savedMessage.getSenderEmail());
            event.put("recipientEmail", recipientEmail);
            event.put("content", savedMessage.getContent());
            event.put("conversationId", savedMessage.getConversationId());

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_MESSAGING,
                    RabbitConfig.ROUTING_KEY_MESSAGE_RECEIVED,
                    event
            );
        } catch (Exception ex) {
            log.warn("Failed to publish message notification event for recipient={}: {}", recipientEmail, ex.getMessage());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
