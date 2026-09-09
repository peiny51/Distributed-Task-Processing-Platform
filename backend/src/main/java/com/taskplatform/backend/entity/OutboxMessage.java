package com.taskplatform.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// OutboxMessage entity represents a message that is stored in the outbox table for eventual processing and publishing to other systems.
// 表示“还有一条消息等待发送到 Redis”
@Entity
@Table(name = "outbox_messages")
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId; // 这个 event 属于哪个业务对象？ business entity id (task id)

    @Column(nullable = false)
    private String payload; // 真正要发送出去的消息内容, 最简单就是taskId, 也可以是 task 的 JSON 序列化对象

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 什么时候被发送到 Redis 了？ null = 还没发送，非 null = 已经发送过了

    public OutboxMessage() {
    }

    public OutboxMessage(String eventType, Long aggregateId, String payload) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}