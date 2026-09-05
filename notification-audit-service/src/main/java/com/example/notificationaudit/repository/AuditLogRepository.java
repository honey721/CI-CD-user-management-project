package com.example.notificationaudit.repository;

import com.example.notificationaudit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Check if event has already been processed for idempotency
    boolean existsByEventId(String eventId);

    // All logs for a specific user
    List<AuditLog> findByUserEmailOrderByProcessedAtDesc(String userEmail);

    // All logs for a specific event type
    List<AuditLog> findByEventTypeOrderByProcessedAtDesc(String eventType);

    // Logs within a time range
    List<AuditLog> findByProcessedAtBetweenOrderByProcessedAtDesc(
            LocalDateTime from, LocalDateTime to);

    // Count events by type
    long countByEventType(String eventType);

    // Count events for a specific user
    long countByUserEmail(String userEmail);

    // Latest N logs across all users
    List<AuditLog> findTop20ByOrderByProcessedAtDesc();

    // Stats: count by event type grouped by day
    @Query("SELECT DATE(a.processedAt) as day, a.eventType, COUNT(a) as total " +
           "FROM AuditLog a " +
           "WHERE a.processedAt >= :since " +
           "GROUP BY DATE(a.processedAt), a.eventType " +
           "ORDER BY day DESC")
    List<Object[]> getDailyStats(@Param("since") LocalDateTime since);
}
