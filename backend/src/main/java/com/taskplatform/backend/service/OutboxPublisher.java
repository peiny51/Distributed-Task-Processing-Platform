//定期扫描还没发送的 OutboxMessage，并把它们发到 Redis。
package com.taskplatform.backend.service;

import com.taskplatform.backend.entity.OutboxMessage;
import com.taskplatform.backend.repository.OutboxMessageRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxPublisher {

    private final OutboxMessageRepository outboxRepository;
    private final TaskQueueService taskQueueService;

    public OutboxPublisher(OutboxMessageRepository outboxRepository, TaskQueueService taskQueueService) {
        this.outboxRepository = outboxRepository;
        this.taskQueueService = taskQueueService;
    }

    @Scheduled(fixedDelay = 1000) // 每隔 1 秒执行一次
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> messages = outboxRepository.findByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxMessage message : messages) {
            if (!message.getEventType().equals("TASK_CREATED")) {
                continue;
            }

            Long taskId = Long.valueOf(message.getPayload());

            taskQueueService.enqueue(taskId);

            message.setPublishedAt(LocalDateTime.now());
        }
    }
}