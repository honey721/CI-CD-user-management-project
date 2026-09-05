package com.example.notificationaudit.service;

import com.example.notificationaudit.entity.AuditLog;
import com.example.notificationaudit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public static final String EVENT_REGISTERED = "USER_REGISTERED";
    public static final String EVENT_LOGGEDIN    = "USER_LOGGEDIN";

    public boolean isEventProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        return auditLogRepository.existsByEventId(eventId);
    }

    public AuditLog saveRegistrationLog(String eventId, Long userId, String email,
                                         String username, String eventTime,
                                         boolean emailSent) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEventId(eventId);
        auditLog.setEventType(EVENT_REGISTERED);
        auditLog.setUserEmail(email);
        auditLog.setUsername(username);
        auditLog.setEventTime(parseEventTime(eventTime));
        auditLog.setProcessedAt(LocalDateTime.now());
        auditLog.setMetadata("{\"userId\": " + userId + "}");
        auditLog.setEmailSent(emailSent);

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Audit log saved for USER_REGISTERED (eventId={}): {}", eventId, email);
        return saved;
    }

    public AuditLog saveLoginLog(String eventId, String email, String username,
                                 String eventTime, boolean emailSent) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEventId(eventId);
        auditLog.setEventType(EVENT_LOGGEDIN);
        auditLog.setUserEmail(email);
        auditLog.setUsername(username);
        auditLog.setEventTime(parseEventTime(eventTime));
        auditLog.setProcessedAt(LocalDateTime.now());
        auditLog.setEmailSent(emailSent);

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Audit log saved for USER_LOGGEDIN (eventId={}): {}", eventId, email);
        return saved;
    }

    private LocalDateTime parseEventTime(String eventTime) {
        try {
            return eventTime != null ? LocalDateTime.parse(eventTime) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public AuditLog saveFailedLog(String eventId, String eventType, String email, String errorMessage) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEventId(eventId);
        auditLog.setEventType(eventType);
        auditLog.setUserEmail(email);
        auditLog.setProcessedAt(LocalDateTime.now());
        auditLog.setEmailSent(false);
        auditLog.setErrorMessage(errorMessage);
        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findTop20ByOrderByProcessedAtDesc();
    }

    public List<AuditLog> getLogsByEmail(String email) {
        return auditLogRepository.findByUserEmailOrderByProcessedAtDesc(email);
    }

    public List<AuditLog> getLogsByEventType(String eventType) {
        return auditLogRepository.findByEventTypeOrderByProcessedAtDesc(eventType);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRegistrations", auditLogRepository.countByEventType(EVENT_REGISTERED));
        stats.put("totalLogins", auditLogRepository.countByEventType(EVENT_LOGGEDIN));
        stats.put("totalEvents", auditLogRepository.count());
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        stats.put("dailyBreakdown", auditLogRepository.getDailyStats(sevenDaysAgo));
        return stats;
    }
}