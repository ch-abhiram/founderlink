package com.notification_service.Service;

import com.notification_service.DTO.InvestmentEvent;
import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvestmentListenerTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private InvestmentListener listener;

    @Test
    void testConsumeInvestmentCreatedSavesInvestorAndFounderNotifications() {
        InvestmentEvent event = new InvestmentEvent();
        event.setInvestorEmail("investor@test.com");
        event.setFounderEmail("founder@test.com");
        event.setStartupId(7L);
        event.setAmount(5000.0);
        event.setStatus("PENDING");

        listener.consumeInvestmentCreated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository, times(2)).save(captor.capture());

        List<Notification> notifications = captor.getAllValues();
        assertEquals(2, notifications.size());
        assertTrue(notifications.stream().anyMatch(n -> "investor@test.com".equals(n.getUserEmail())));
        assertTrue(notifications.stream().anyMatch(n -> "founder@test.com".equals(n.getUserEmail())));
    }

    @Test
    void testConsumeInvestmentStatusSavesNotification() {
        InvestmentEvent event = new InvestmentEvent();
        event.setInvestorEmail("investor@test.com");
        event.setStartupId(7L);
        event.setAmount(5000.0);
        event.setStatus("APPROVED");

        listener.consumeInvestmentStatus(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification notification = captor.getValue();
        assertEquals("investor@test.com", notification.getUserEmail());
        assertEquals("INVESTMENT", notification.getType());
        assertEquals("Investment Status Update", notification.getTitle());
    }
}
