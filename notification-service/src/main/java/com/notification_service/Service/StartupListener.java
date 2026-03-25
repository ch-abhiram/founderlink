package com.notification_service.Service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.notification_service.Config.RabbitConfig;
import com.notification_service.DTO.StartupEvent;
import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartupListener {

    private final NotificationRepository repository;

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFY_STARTUP_CREATED)
    public void consumeStartupCreated(StartupEvent event) {
        log.info("Received startup created event for founderEmail={}, startupId={}",
                event.getFounderEmail(), event.getStartupId());

        Notification notification = new Notification();
        notification.setUserEmail(event.getFounderEmail());
        notification.setType("SYSTEM");
        notification.setTitle("Startup Registered");
        notification.setMessage(
                "Your startup '" + event.getName() + "' (ID: " + event.getStartupId() +
                ") has been registered successfully. Its status is currently " + event.getStatus() + "."
        );

        repository.save(notification);
    }
}
