package com.notification_service.Service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.notification_service.Config.RabbitConfig;
import com.notification_service.DTO.TeamInviteEvent;
import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamListener {

    private final NotificationRepository repository;

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFY_TEAM_INVITE)
    public void consumeTeamInvite(TeamInviteEvent event) {
        log.info("Received team invite event for userEmail={}, startupName={}",
                event.getUserEmail(), event.getStartupName());

        Notification notification = new Notification();
        notification.setUserEmail(event.getUserEmail());
        notification.setType("TEAM");
        notification.setTitle("New Team Invitation");
        notification.setMessage(
                "You have been invited to join the startup '" + event.getStartupName() +
                "' as a " + event.getRole() + ". Please review your invitations."
        );

        repository.save(notification);
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFY_TEAM_STATUS)
    public void consumeTeamInviteStatus(TeamInviteEvent event) {
        if (event.getFounderEmail() == null || event.getFounderEmail().isBlank()) {
            log.warn("Skipping team invite status notification because founderEmail is missing for inviteId={}", event.getInviteId());
            return;
        }

        Notification notification = new Notification();
        notification.setUserEmail(event.getFounderEmail());
        notification.setType("TEAM");
        notification.setTitle("Team Invitation Updated");
        notification.setMessage(
                event.getUserEmail() + " has " + event.getStatus().toLowerCase() +
                " the invitation for startup '" + event.getStartupName() + "'."
        );

        repository.save(notification);
    }
}
