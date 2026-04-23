package com.notification_service.Config;

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

    public static final String EXCHANGE_INVESTMENT = "investment.exchange";
    public static final String EXCHANGE_STARTUP = "startup.exchange";
    public static final String EXCHANGE_TEAM = "team.exchange";
    public static final String EXCHANGE_MESSAGING = "messaging.exchange";

    public static final String QUEUE_NOTIFY_INVESTMENT_CREATED = "notify.investment.created.queue";
    public static final String QUEUE_NOTIFY_INVESTMENT_STATUS = "notify.investment.status.queue";
    public static final String QUEUE_NOTIFY_STARTUP_CREATED = "notify.startup.created.queue";
    public static final String QUEUE_NOTIFY_TEAM_INVITE = "notify.team.invite.queue";
    public static final String QUEUE_NOTIFY_TEAM_STATUS = "notify.team.status.queue";
    public static final String QUEUE_NOTIFY_MESSAGE_REPLY = "notify.message.reply.queue";

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
    public DirectExchange investmentExchange() {
        return new DirectExchange(EXCHANGE_INVESTMENT, true, false);
    }

    @Bean
    public DirectExchange startupExchange() {
        return new DirectExchange(EXCHANGE_STARTUP, true, false);
    }

    @Bean
    public DirectExchange teamExchange() {
        return new DirectExchange(EXCHANGE_TEAM, true, false);
    }

    @Bean
    public DirectExchange messagingExchange() {
        return new DirectExchange(EXCHANGE_MESSAGING, true, false);
    }

    @Bean
    public Queue notifyInvestmentCreatedQueue() {
        return new Queue(QUEUE_NOTIFY_INVESTMENT_CREATED, true);
    }

    @Bean
    public Queue notifyInvestmentStatusQueue() {
        return new Queue(QUEUE_NOTIFY_INVESTMENT_STATUS, true);
    }

    @Bean
    public Queue notifyStartupCreatedQueue() {
        return new Queue(QUEUE_NOTIFY_STARTUP_CREATED, true);
    }

    @Bean
    public Queue notifyTeamInviteQueue() {
        return new Queue(QUEUE_NOTIFY_TEAM_INVITE, true);
    }

    @Bean
    public Queue notifyTeamStatusQueue() {
        return new Queue(QUEUE_NOTIFY_TEAM_STATUS, true);
    }

    @Bean
    public Queue notifyMessageReplyQueue() {
        return new Queue(QUEUE_NOTIFY_MESSAGE_REPLY, true);
    }

    @Bean
    public Binding bindingNotifyInvestmentCreated() {
        return BindingBuilder.bind(notifyInvestmentCreatedQueue()).to(investmentExchange()).with("investment.created");
    }

    @Bean
    public Binding bindingNotifyInvestmentStatus() {
        return BindingBuilder.bind(notifyInvestmentStatusQueue()).to(investmentExchange()).with("investment.status");
    }

    @Bean
    public Binding bindingNotifyStartupCreated() {
        return BindingBuilder.bind(notifyStartupCreatedQueue()).to(startupExchange()).with("startup.created");
    }

    @Bean
    public Binding bindingNotifyTeamInvite() {
        return BindingBuilder.bind(notifyTeamInviteQueue()).to(teamExchange()).with("team.invite.sent");
    }

    @Bean
    public Binding bindingNotifyTeamStatus() {
        return BindingBuilder.bind(notifyTeamStatusQueue()).to(teamExchange()).with("team.invite.status");
    }

    @Bean
    public Binding bindingNotifyMessageReply() {
        return BindingBuilder.bind(notifyMessageReplyQueue()).to(messagingExchange()).with("message.reply.founder");
    }
}
