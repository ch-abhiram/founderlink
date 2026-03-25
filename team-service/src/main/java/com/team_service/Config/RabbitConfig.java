package com.team_service.Config;

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

    public static final String EXCHANGE = "team.exchange";
    public static final String ROUTING_KEY_INVITE = "team.invite.sent";
    public static final String ROUTING_KEY_STATUS = "team.invite.status";

    public static final String QUEUE_INVITE = "team.invite.queue";
    public static final String QUEUE_STATUS = "team.status.queue";

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue inviteQueue() {
        return new Queue(QUEUE_INVITE, true);
    }
    
    @Bean
    public Queue statusQueue() {
        return new Queue(QUEUE_STATUS, true);
    }

    @Bean
    public Binding bindingInvite(Queue inviteQueue, DirectExchange exchange) {
        return BindingBuilder.bind(inviteQueue).to(exchange).with(ROUTING_KEY_INVITE);
    }
    
    @Bean
    public Binding bindingStatus(Queue statusQueue, DirectExchange exchange) {
        return BindingBuilder.bind(statusQueue).to(exchange).with(ROUTING_KEY_STATUS);
    }
}
