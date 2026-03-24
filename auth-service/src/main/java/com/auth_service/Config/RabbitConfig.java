package com.auth_service.Config;



import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;





@Configuration
public class RabbitConfig {

    public static final String QUEUE = "user.events";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }
}