package com.example.notificationaudit.listener;

import com.example.notificationaudit.dto.UserLoggedInEvent;
import com.example.notificationaudit.dto.UserRegisteredEvent;
import com.example.notificationaudit.service.AuditService;
import com.example.notificationaudit.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);

    private final EmailService emailService;
    private final AuditService auditService;

    @Autowired
    public UserEventListener(EmailService emailService, AuditService auditService) {
        this.emailService = emailService;
        this.auditService = auditService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.registered}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("[USER_REGISTERED] Received event for: {} (eventId: {}, userId: {})",
                event.getEmail(), event.getEventId(), event.getId());

        // Idempotency Check
        if (auditService.isEventProcessed(event.getEventId())) {
            log.warn("[USER_REGISTERED] Duplicate event detected (eventId={}). Skipping processing.",
                    event.getEventId());
            return;
        }

        try {
            boolean emailSent = emailService.sendWelcomeEmail(
                    event.getEmail(), event.getUsername(), event.getTime());

            auditService.saveRegistrationLog(
                    event.getEventId(), event.getId(), event.getEmail(),
                    event.getUsername(), event.getTime(), emailSent);

            log.info("[USER_REGISTERED] Fully processed for: {} (eventId={})",
                    event.getEmail(), event.getEventId());

        } catch (Exception e) {
            log.error("[USER_REGISTERED] Failed for {}: {}", event.getEmail(), e.getMessage());
            auditService.saveFailedLog(event.getEventId(), AuditService.EVENT_REGISTERED,
                    event.getEmail(), e.getMessage());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.loggedin}")
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("[USER_LOGGEDIN] Received event for: {} (eventId: {})",
                event.getEmail(), event.getEventId());

        // Idempotency Check
        if (auditService.isEventProcessed(event.getEventId())) {
            log.warn("[USER_LOGGEDIN] Duplicate event detected (eventId={}). Skipping processing.",
                    event.getEventId());
            return;
        }

        try {
            boolean emailSent = emailService.sendLoginAlertEmail(
                    event.getEmail(), event.getUsername(), event.getTime());

            auditService.saveLoginLog(
                    event.getEventId(), event.getEmail(), event.getUsername(),
                    event.getTime(), emailSent);

            log.info("[USER_LOGGEDIN] Fully processed for: {} (eventId={})",
                    event.getEmail(), event.getEventId());

        } catch (Exception e) {
            log.error("[USER_LOGGEDIN] Failed for {}: {}", event.getEmail(), e.getMessage());
            auditService.saveFailedLog(event.getEventId(), AuditService.EVENT_LOGGEDIN,
                    event.getEmail(), e.getMessage());
        }
    }
}