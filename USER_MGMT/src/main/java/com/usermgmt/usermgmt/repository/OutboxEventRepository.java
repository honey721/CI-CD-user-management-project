package com.usermgmt.usermgmt.repository;

import com.usermgmt.usermgmt.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(String status);

    List<OutboxEvent> findTop50ByStatusAndRetryCountLessThanOrderByCreatedAtAsc(String status, int maxRetries);
}
