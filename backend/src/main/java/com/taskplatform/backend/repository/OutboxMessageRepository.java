package com.taskplatform.backend.repository;

import com.taskplatform.backend.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// SELECT *
// FROM outbox_messages
// WHERE published_at IS NULL
// ORDER BY created_at ASC;
//找所有还没发布的消息，最早的优先。
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findByPublishedAtIsNullOrderByCreatedAtAsc();
}