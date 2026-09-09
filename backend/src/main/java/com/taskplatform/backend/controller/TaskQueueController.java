package com.taskplatform.backend.controller;

import com.taskplatform.backend.service.TaskQueueService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class TaskQueueController {

    private final TaskQueueService taskQueueService;

    public TaskQueueController(TaskQueueService taskQueueService) {
        this.taskQueueService = taskQueueService;
    }

    @PostMapping("/{taskId}")
    public void enqueue(@PathVariable Long taskId) {
        taskQueueService.enqueue(taskId);
    }

    @PostMapping("/dequeue")
    public Long dequeue() {
        return taskQueueService.dequeue();
    }
}