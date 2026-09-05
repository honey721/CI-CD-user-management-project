package com.example.notificationaudit.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * RabbitMQ configuration for the consumer service.
 *
 * This declares the same exchange that USER_MGMT publishes to,
 * creates dedicated queues for this service, and binds them
 * with the correct routing keys.
 *
 * Exchange:    user.events.exchange  (must match producer)
 * Queue 1:     user.registered.queue → routing key: user.registered
 * Queue 2:     user.loggedin.queue   → routing key: user.loggedin
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.registered}")
    private String registeredQueue;

    @Value("${rabbitmq.queue.loggedin}")
    private String loggedinQueue;

    @Value("${rabbitmq.routingkey.registered}")
    private String registeredRoutingKey;

    @Value("${rabbitmq.routingkey.loggedin}")
    private String loggedinRoutingKey;

    // ── Exchange ────────────────────────────────────────────────────────────
    // Topic exchange — must match the exchange declared in USER_MGMT producer
    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(exchange);
    }
    
    // ── Queues ──────────────────────────────────────────────────────────────
    // Durable = true so queues survive RabbitMQ restarts
    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(registeredQueue).build();
    }

    @Bean
    public Queue userLoggedInQueue() {
        return QueueBuilder.durable(loggedinQueue).build();
    }

    // ── Bindings ────────────────────────────────────────────────────────────
    // Bind each queue to the exchange with its routing key
    @Bean
    public Binding bindingRegistered(Queue userRegisteredQueue,
                                      TopicExchange userEventsExchange) {
        return BindingBuilder
                .bind(userRegisteredQueue)
                .to(userEventsExchange)
                .with(registeredRoutingKey);
    }

    @Bean
    public Binding bindingLoggedIn(Queue userLoggedInQueue,
                                    TopicExchange userEventsExchange) {
        return BindingBuilder
                .bind(userLoggedInQueue)
                .to(userEventsExchange)
                .with(loggedinRoutingKey);
    }

    // ── JSON Message Converter ───────────────────────────────────────────────
    // Automatically deserializes JSON payloads into Java objects
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
