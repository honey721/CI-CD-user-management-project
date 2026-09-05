package com.example.notificationaudit.controller;

import com.example.notificationaudit.entity.AuditLog;
import com.example.notificationaudit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API to query audit logs.
 * These endpoints are useful for admins to see what's happening in the system.
 *
 * All endpoints run on port 8082 (this service)
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditService auditService;
    
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }
    
    /**
     * GET /audit/logs
     * Returns the 20 most recent audit logs across all users and event types.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    /**
     * GET /audit/logs/user/{email}
     * Returns all audit logs for a specific user email.
     *
     * Example: GET /audit/logs/user/john@example.com
     */
    @GetMapping("/logs/user/{email}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(
            @PathVariable String email) {
        List<AuditLog> logs = auditService.getLogsByEmail(email);
        if (logs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /audit/logs/type/{eventType}
     * Returns logs filtered by event type: USER_REGISTERED or USER_LOGGEDIN
     *
     * Example: GET /audit/logs/type/USER_REGISTERED
     */
    @GetMapping("/logs/type/{eventType}")
    public ResponseEntity<List<AuditLog>> getLogsByType(
            @PathVariable String eventType) {
        return ResponseEntity.ok(
                auditService.getLogsByEventType(eventType.toUpperCase()));
    }

    /**
     * GET /audit/stats
     * Returns aggregated statistics:
     * - Total registrations
     * - Total logins
     * - Total events
     * - Daily breakdown (last 7 days)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(auditService.getStats());
    }

    /**
     * GET /audit/health
     * Simple health check for the service.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-audit-service"
        ));
    }
}
