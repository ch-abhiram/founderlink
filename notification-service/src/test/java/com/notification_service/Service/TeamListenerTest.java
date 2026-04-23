package com.notification_service.Service;

import com.notification_service.DTO.TeamInviteEvent;
import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamListenerTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private TeamListener listener;

    @Test
    void testConsumeTeamInviteSavesNotification() {
        TeamInviteEvent event = new TeamInviteEvent();
        event.setUserEmail("user@test.com");
        event.setStartupName("MyStartup");
        event.setRole("ADVISOR");

        listener.consumeTeamInvite(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification notification = captor.getValue();
        assertEquals("user@test.com", notification.getUserEmail());
        assertEquals("TEAM", notification.getType());
        assertEquals("New Team Invitation", notification.getTitle());
    }

    @Test
    void testConsumeTeamInviteStatusSavesFounderNotification() {
        TeamInviteEvent event = new TeamInviteEvent();
        event.setInviteId(42L);
        event.setFounderEmail("founder@test.com");
        event.setUserEmail("user@test.com");
        event.setStatus("ACCEPTED");
        event.setStartupName("MyStartup");

        listener.consumeTeamInviteStatus(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification notification = captor.getValue();
        assertEquals("founder@test.com", notification.getUserEmail());
        assertEquals("TEAM", notification.getType());
        assertEquals("Team Invitation Updated", notification.getTitle());
    }

    @Test
    void testConsumeTeamInviteStatusSkipsWhenFounderMissing() {
        TeamInviteEvent event = new TeamInviteEvent();
        event.setInviteId(42L);
        event.setUserEmail("user@test.com");
        event.setStatus("REJECTED");
        event.setStartupName("MyStartup");

        listener.consumeTeamInviteStatus(event);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
