package com.taskplatform.backend.controller;

import com.taskplatform.backend.dto.CreateTaskRequest;
import com.taskplatform.backend.entity.ProcessingTask;
import com.taskplatform.backend.entity.TaskHistory;
import com.taskplatform.backend.entity.TaskResult;
import com.taskplatform.backend.service.ProcessingTaskService;
import com.taskplatform.backend.entity.TaskHistory;
import com.taskplatform.backend.entity.TaskResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class ProcessingTaskController {

    private final ProcessingTaskService service;

    public ProcessingTaskController(ProcessingTaskService service) {
        this.service = service;
    }

    // request body: { "taskType": "DATA_CLEANING" }
    // request header: Idempotency-Key: 12345 从 HTTP request header 里读取
    // Idempotency-Key，然后传给这个参数
    @PostMapping
    public ProcessingTask createTask(@Valid @RequestBody CreateTaskRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return service.createTask(request.getTaskType(), request.getInputPath(), idempotencyKey);
    }

    @GetMapping
    public List<ProcessingTask> getAllTasks() {
        return service.getAllTasks();
    }

    @GetMapping("/{id}")
    public ProcessingTask getTaskById(@PathVariable Long id) {
        return service.getTaskById(id);
    }

    @PostMapping("/{id}/complete")
    public ProcessingTask completeTask(@PathVariable Long id, @RequestParam String outputPath) {
        return service.completeTask(id, outputPath);
    }

    @PostMapping("/claim-next")
    public ProcessingTask claimNextTask() {
        return service.claimNextTask();
    }

    @GetMapping("/{id}/history")
    public List<TaskHistory> getTaskHistory(@PathVariable Long id) {
        return service.getTaskHistory(id);
    }

    @GetMapping("/{id}/result")
    public TaskResult getTaskResult(@PathVariable Long id) {
        return service.getTaskResult(id);
    }
}