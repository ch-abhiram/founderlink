package com.startup_service.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
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
