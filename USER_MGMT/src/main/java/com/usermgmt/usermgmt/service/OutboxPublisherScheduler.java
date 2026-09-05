package com.usermgmt.usermgmt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermgmt.usermgmt.entity.OutboxEvent;
import com.usermgmt.usermgmt.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxEventRepository outboxRepo;
    private final EventPublisherService eventPublisher;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 5;

    public OutboxPublisherScheduler(OutboxEventRepository outboxRepo,
                                    EventPublisherService eventPublisher,
                                    ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 3000)
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents =
                outboxRepo.findTop50ByStatusAndRetryCountLessThanOrderByCreatedAtAsc("PENDING", MAX_RETRIES);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox event(s) to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Deserialize stored JSON payload back to Map so Jackson2JsonMessageConverter handles it cleanly
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadMap = objectMapper.readValue(event.getPayload(), Map.class);

                eventPublisher.publishDirectly(event.getRoutingKey(), payloadMap);

                event.setStatus("PROCESSED");
                event.setProcessedAt(LocalDateTime.now());
                event.setErrorMessage(null);
                log.info("Successfully published outbox eventId={} (type={})",
                        event.getEventId(), event.getEventType());

            } catch (Exception e) {
                int newRetryCount = event.getRetryCount() + 1;
                event.setRetryCount(newRetryCount);
                event.setErrorMessage(e.getMessage());

                if (newRetryCount >= MAX_RETRIES) {
                    event.setStatus("FAILED");
                    log.error("Outbox eventId={} reached max retries ({}) and marked FAILED: {}",
                            event.getEventId(), MAX_RETRIES, e.getMessage());
                } else {
                    log.warn("Failed to publish outbox eventId={} (retry {}/{}): {}",
                            event.getEventId(), newRetryCount, MAX_RETRIES, e.getMessage());
                }
            }
            outboxRepo.save(event);
        }
    }
}
