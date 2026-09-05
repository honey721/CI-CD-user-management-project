package com.usermgmt.usermgmt.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "user.events.exchange";
    public static final String REG_ROUTE = "user.registered";
    public static final String LOGIN_ROUTE = "user.loggedin";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue registeredQueue() {
        return new Queue("user.registered.queue");
    }

    @Bean
    public Queue loginQueue() {
        return new Queue("user.loggedin.queue");
    }

    @Bean
    public Binding registeredBinding(Queue registeredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(registeredQueue).to(userExchange).with(REG_ROUTE);
    }

    @Bean
    public Binding loginBinding(Queue loginQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(loginQueue).to(userExchange).with(LOGIN_ROUTE);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
    
}
