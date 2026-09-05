package com.usermgmt.usermgmt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermgmt.usermgmt.entity.OutboxEvent;
import com.usermgmt.usermgmt.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(EventPublisherService.class);

    private final RabbitTemplate rabbitTemplate;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    private final String exchange = "user.events.exchange";
    private final String registrationRoutingKey = "user.registered";
    private final String loginRoutingKey = "user.loggedin";

    public EventPublisherService(RabbitTemplate rabbitTemplate,
                                 OutboxEventRepository outboxRepo,
                                 ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createRegistrationOutboxEvent(Map<String, Object> payload) {
        saveToOutbox("USER_REGISTERED", registrationRoutingKey, payload);
    }

    @Transactional
    public void createLoginOutboxEvent(Map<String, Object> payload) {
        saveToOutbox("USER_LOGGEDIN", loginRoutingKey, payload);
    }

    private void saveToOutbox(String eventType, String routingKey, Map<String, Object> payloadMap) {
        try {
            Map<String, Object> enrichedPayload = new HashMap<>(payloadMap);
            String eventId = UUID.randomUUID().toString();
            enrichedPayload.put("eventId", eventId);

            String jsonPayload = objectMapper.writeValueAsString(enrichedPayload);

            OutboxEvent outboxEvent = new OutboxEvent(eventId, eventType, routingKey, jsonPayload);
            outboxRepo.save(outboxEvent);
            log.info("Saved outbox event to DB: type={}, eventId={}", eventType, eventId);
        } catch (Exception e) {
            log.error("Failed to save outbox event for eventType={}: {}", eventType, e.getMessage(), e);
            throw new RuntimeException("Failed to persist outbox event", e);
        }
    }

    public void publishDirectly(String routingKey, Object payload) {
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }
}
