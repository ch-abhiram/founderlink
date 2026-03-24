package com.auth_service.Service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    @RabbitListener(queues = "user.events")
    public void handleUserLogin(String email) {
        System.out.println("User logged in: " + email);
    }
}
