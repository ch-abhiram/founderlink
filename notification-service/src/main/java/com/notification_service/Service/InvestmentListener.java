package com.notification_service.Service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.notification_service.Config.RabbitConfig;
import com.notification_service.DTO.InvestmentEvent;
import com.notification_service.Entity.Notification;
import com.notification_service.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentListener {

    private final NotificationRepository repository;

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFY_INVESTMENT_CREATED)
    public void consumeInvestmentCreated(InvestmentEvent event) {
        log.info("Received investment created event for investorEmail={}, startupId={}",
                event.getInvestorEmail(), event.getStartupId());

        Notification investorNotification = new Notification();
        investorNotification.setUserEmail(event.getInvestorEmail());
        investorNotification.setType("INVESTMENT");
        investorNotification.setTitle("Investment Initiated");
        investorNotification.setMessage(
                "You successfully initiated an investment of " + event.getAmount() +
                " in startup ID " + event.getStartupId() + ". Current status is " + event.getStatus() + "."
        );
        repository.save(investorNotification);

        if (event.getFounderEmail() != null && !event.getFounderEmail().isBlank()) {
            Notification founderNotification = new Notification();
            founderNotification.setUserEmail(event.getFounderEmail());
            founderNotification.setType("INVESTMENT");
            founderNotification.setTitle("New Investment Received");
            founderNotification.setMessage(
                    "Your startup ID " + event.getStartupId() + " received a new investment of " +
                    event.getAmount() + " from " + event.getInvestorEmail() + "."
            );
            repository.save(founderNotification);
        }
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFY_INVESTMENT_STATUS)
    public void consumeInvestmentStatus(InvestmentEvent event) {
        log.info("Received investment status event for investorEmail={}, startupId={}, status={}",
                event.getInvestorEmail(), event.getStartupId(), event.getStatus());

        Notification notification = new Notification();
        notification.setUserEmail(event.getInvestorEmail());
        notification.setType("INVESTMENT");
        notification.setTitle("Investment Status Update");
        notification.setMessage(
                "Your investment of " + event.getAmount() +
                " in startup ID " + event.getStartupId() + " is now marked as " + event.getStatus() + "."
        );

        repository.save(notification);
    }
}
