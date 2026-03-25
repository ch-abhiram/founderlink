package com.messaging_service.Controller;

import com.messaging_service.DTO.ConversationResponseDTO;
import com.messaging_service.DTO.MessageResponseDTO;
import com.messaging_service.DTO.SendMessageRequest;
import com.messaging_service.Service.MessagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService service;

    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(@RequestBody @Valid SendMessageRequest request) {
        return ResponseEntity.ok(service.sendMessage(request));
    }

    @GetMapping("/conversation/{id}")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMessages(id));
    }

    @GetMapping("/startup/{startupId}")
    public ResponseEntity<List<ConversationResponseDTO>> getStartupConversations(@PathVariable Long startupId) {
        return ResponseEntity.ok(service.getStartupConversations(startupId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ConversationResponseDTO>> getMyConversations() {
        return ResponseEntity.ok(service.getMyConversations());
    }
}
