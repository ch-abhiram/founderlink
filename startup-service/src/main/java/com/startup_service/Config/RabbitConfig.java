package com.startup_service.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "investment.exchange";
    public static final String APPROVED_QUEUE = "startup.investment.approved";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue approvedQueue() {
        return new Queue(APPROVED_QUEUE, true);
    }

    @Bean
    public Binding bindingApproved(Queue approvedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(approvedQueue)
                .to(exchange)
                .with("investment.status");
    }
}
