package com.notification_service.Service;

import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification1;
    private Notification notification2;

    @BeforeEach
    void setUp() {
        notification1 = new Notification(1L, "user@test.com", "Title 1", "Message 1", "SYSTEM", "UNREAD", LocalDateTime.now());
        notification2 = new Notification(2L, "user@test.com", "Title 2", "Message 2", "TEAM", "READ", LocalDateTime.now());
    }

    private void setupSecurityContext(String email) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetMyNotifications_All() {
        setupSecurityContext("user@test.com");
        when(repository.findByUserEmailOrderByCreatedAtDesc("user@test.com"))
                .thenReturn(Arrays.asList(notification1, notification2));

        List<Notification> result = notificationService.getMyNotifications(false);

        assertEquals(2, result.size());
        verify(repository).findByUserEmailOrderByCreatedAtDesc("user@test.com");
    }

    @Test
    void testGetMyNotifications_UnreadOnly() {
        setupSecurityContext("user@test.com");
        when(repository.findByUserEmailAndStatusOrderByCreatedAtDesc("user@test.com", "UNREAD"))
                .thenReturn(Collections.singletonList(notification1));

        List<Notification> result = notificationService.getMyNotifications(true);

        assertEquals(1, result.size());
        assertEquals("UNREAD", result.get(0).getStatus());
        verify(repository).findByUserEmailAndStatusOrderByCreatedAtDesc("user@test.com", "UNREAD");
    }

    @Test
    void testMarkAsRead_Success() {
        setupSecurityContext("user@test.com");
        when(repository.findById(1L)).thenReturn(Optional.of(notification1));
        when(repository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification result = notificationService.markAsRead(1L);

        assertEquals("READ", result.getStatus());
        verify(repository).save(notification1);
    }

    @Test
    void testMarkAsRead_Forbidden() {
        setupSecurityContext("other@test.com");
        when(repository.findById(1L)).thenReturn(Optional.of(notification1));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            notificationService.markAsRead(1L);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void testMarkAllAsRead() {
        setupSecurityContext("user@test.com");
        when(repository.findByUserEmailAndStatusOrderByCreatedAtDesc("user@test.com", "UNREAD"))
                .thenReturn(Collections.singletonList(notification1));

        notificationService.markAllAsRead();

        assertEquals("READ", notification1.getStatus());
        verify(repository).saveAll(any());
    }
}
