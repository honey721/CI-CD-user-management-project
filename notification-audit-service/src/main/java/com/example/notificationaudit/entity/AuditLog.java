package com.example.notificationaudit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_event_id", columnList = "eventId")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String userEmail;

    private String username;

    private LocalDateTime eventTime;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    private Boolean emailSent = false;

    private String errorMessage;

    // ── No-arg constructor (required by JPA) ────────────────
    public AuditLog() {}

    // ── Getters ──────────────────────────────────────────────
    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getUserEmail() { return userEmail; }
    public String getUsername() { return username; }
    public LocalDateTime getEventTime() { return eventTime; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public String getMetadata() { return metadata; }
    public Boolean getEmailSent() { return emailSent; }
    public String getErrorMessage() { return errorMessage; }

    // ── Setters ──────────────────────────────────────────────
    public void setId(Long id) { this.id = id; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setUsername(String username) { this.username = username; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setEmailSent(Boolean emailSent) { this.emailSent = emailSent; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}