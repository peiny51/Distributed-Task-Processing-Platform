package com.taskplatform.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Queue;

@Service
public class TaskQueueService {

    private static final String TASK_QUEUE = "task_queue"; // Redis list key for the task queue

    private final StringRedisTemplate redisTemplate;

    public TaskQueueService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void enqueue(Long taskId) {
        redisTemplate.opsForList().leftPush(TASK_QUEUE, taskId.toString()); //FIFO
    }

//     Queue 有数据
// → 立刻返回

// Queue 没数据
// → 等待最多 5 秒
// → 期间如果有 task 进来，立刻返回
// → 5 秒还没有，返回 null
    public Long dequeue() {
        // 从 Redis List 右边拿一个元素，如果暂时没有，就最多等 5 秒
        String taskId = redisTemplate.opsForList().rightPop(TASK_QUEUE, Duration.ofSeconds(5));

        if (taskId == null) {
            return null;
        }

        return Long.valueOf(taskId);
    }
}