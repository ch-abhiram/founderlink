package com.notification_service.Service;

import com.notification_service.DTO.StartupEvent;
import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartupListenerTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private StartupListener listener;

    @Test
    void testConsumeStartupCreatedSavesNotification() {
        StartupEvent event = new StartupEvent();
        event.setFounderEmail("founder@test.com");
        event.setStartupId(1L);
        event.setName("MyStartup");
        event.setStatus("PENDING");

        listener.consumeStartupCreated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification notification = captor.getValue();
        assertEquals("founder@test.com", notification.getUserEmail());
        assertEquals("SYSTEM", notification.getType());
        assertEquals("Startup Registered", notification.getTitle());
    }
}
