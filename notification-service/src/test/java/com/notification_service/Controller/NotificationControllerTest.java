package com.notification_service.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.notification_service.Entity.Notification;
import com.notification_service.Service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService service;

    private NotificationController controller;
    private Notification notification;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(service);
        notification = notification();
    }

    @Test
    void getMyNotificationsMapsEntities() {
        when(service.getMyNotifications(true)).thenReturn(List.of(notification));

        var response = controller.getMyNotifications(true);

        assertEquals(1, response.getBody().size());
        assertEquals("Investment received", response.getBody().get(0).getTitle());
        assertEquals("UNREAD", response.getBody().get(0).getStatus());
    }

    @Test
    void markAsReadMapsUpdatedNotification() {
        notification.setStatus("READ");
        when(service.markAsRead(4L)).thenReturn(notification);

        assertEquals("READ", controller.markAsRead(4L).getBody().getStatus());
    }

    @Test
    void markAllAsReadReturnsNoContent() {
        assertEquals(HttpStatus.NO_CONTENT, controller.markAllAsRead().getStatusCode());
        verify(service).markAllAsRead();
    }

    private Notification notification() {
        Notification value = new Notification();
        value.setId(4L);
        value.setUserEmail("founder@test.com");
        value.setTitle("Investment received");
        value.setMessage("Someone invested");
        value.setType("INVESTMENT");
        value.setStatus("UNREAD");
        value.setCreatedAt(LocalDateTime.now());
        return value;
    }
}
