package com.notification_service.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.notification_service.DTO.NotificationResponseDTO;
import com.notification_service.Service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(service.getMyNotifications(unreadOnly).stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(service.markAsRead(id)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        service.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    private NotificationResponseDTO toDto(com.notification_service.Entity.Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setUserEmail(notification.getUserEmail());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setStatus(notification.getStatus());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
