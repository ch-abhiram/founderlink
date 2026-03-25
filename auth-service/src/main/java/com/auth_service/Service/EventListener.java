package com.auth_service.Service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventListener {

    @RabbitListener(queues = "user.events")
    public void handleUserLogin(String email) {
        log.info("User login event received for email={}", email);
    }
}
