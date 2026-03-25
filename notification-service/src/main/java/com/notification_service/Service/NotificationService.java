package com.notification_service.Service;

import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public List<Notification> getMyNotifications(boolean unreadOnly) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (unreadOnly) {
            return repository.findByUserEmailAndStatusOrderByCreatedAtDesc(currentUser, "UNREAD");
        }
        return repository.findByUserEmailOrderByCreatedAtDesc(currentUser);
    }

    @Transactional
    public Notification markAsRead(Long id) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
                
        if (!notification.getUserEmail().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this notification");
        }
        
        notification.setStatus("READ");
        return repository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Notification> unread = repository.findByUserEmailAndStatusOrderByCreatedAtDesc(currentUser, "UNREAD");
        
        unread.forEach(n -> n.setStatus("READ"));
        repository.saveAll(unread);
    }
}
