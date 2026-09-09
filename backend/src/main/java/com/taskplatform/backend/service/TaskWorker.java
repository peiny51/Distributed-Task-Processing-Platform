package com.taskplatform.backend.service;

import com.taskplatform.backend.entity.ProcessingTask;
import com.taskplatform.backend.entity.TaskHistory;
import com.taskplatform.backend.entity.TaskResult;
import com.taskplatform.backend.entity.TaskStatus;
import com.taskplatform.backend.entity.TaskType;
import com.taskplatform.backend.repository.ProcessingTaskRepository;
import com.taskplatform.backend.repository.TaskHistoryRepository;
import com.taskplatform.backend.repository.TaskResultRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class TaskWorker {

    private static final int MAX_RETRIES = 3;
    private static final int STALE_TASK_SECONDS = 30;

    private final TaskQueueService taskQueueService;
    private final ProcessingTaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;
    private final TaskResultRepository resultRepository;
    private final TransactionTemplate transactionTemplate;

    public TaskWorker(TaskQueueService taskQueueService, ProcessingTaskRepository taskRepository,
            TaskHistoryRepository historyRepository, TaskResultRepository resultRepository,
            TransactionTemplate transactionTemplate) {
        this.taskQueueService = taskQueueService;
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.resultRepository = resultRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void processNextTask() {
        Long taskId = taskQueueService.dequeue();

        if (taskId == null) {
            return;
        }

        try {
            // 如果 startTask 返回 false，说明这个 task 已经被处理过了，直接返回，不再处理
            if (!startTask(taskId)) {
                return;
            }

            String outputPath = processTask(taskId);
            completeTask(taskId, outputPath);
        } catch (Exception e) {
            handleFailure(taskId, e);
        }
    }

    public boolean startTask(Long taskId) {
        Boolean started = transactionTemplate.execute(status -> {
            ProcessingTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            // 如果 Redis 里因为 at-least-once delivery 出现重复 taskId, 那么这里就会返回 false, 不会重复处理
            // 第一次已经Task 25 → PROCESSING ，第二次Task 25 → PROCESSING，返回 false 就不会重复 start
            if (task.getStatus() != TaskStatus.QUEUED) {
                return false;
            }

            task.setStatus(TaskStatus.PROCESSING);
            task.setStartedAt(LocalDateTime.now());

            historyRepository.save(new TaskHistory(task, TaskStatus.PROCESSING));

            return true;
        });

        return Boolean.TRUE.equals(started);
    }

    public String processTask(Long taskId) throws IOException {
        ProcessingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getTaskType() != TaskType.DATA_CLEANING) {
            throw new RuntimeException("Unsupported task type");
        }

        Path inputPath = Path.of(task.getInputPath());
        Path outputPath = Path.of("/tmp/cleaned-" + taskId + ".csv");

        List<String> lines = Files.readAllLines(inputPath);

        List<String> cleanedLines = lines.stream()
                .filter(line -> !line.trim().isEmpty())
                .toList();

        Files.write(outputPath, cleanedLines);

        return outputPath.toString();
    }

    public void completeTask(Long taskId, String outputPath) {
        transactionTemplate.execute(status -> {
            ProcessingTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            task.setStatus(TaskStatus.COMPLETED);
            task.setProgress(100);
            task.setCompletedAt(LocalDateTime.now());

            resultRepository.save(new TaskResult(task, outputPath));
            historyRepository.save(new TaskHistory(task, TaskStatus.COMPLETED));

            return null;
        });
    }

    public void handleFailure(Long taskId, Exception e) {
        Boolean shouldRetry = transactionTemplate.execute(status -> {
            ProcessingTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            int retryCount = task.getRetryCount() + 1;

            task.setRetryCount(retryCount);
            task.setErrorMessage(e.getMessage());

            if (retryCount >= MAX_RETRIES) {
                task.setStatus(TaskStatus.FAILED);
                historyRepository.save(new TaskHistory(task, TaskStatus.FAILED));
                return false;
            }

            task.setStatus(TaskStatus.QUEUED);
            historyRepository.save(new TaskHistory(task, TaskStatus.QUEUED));

            return true;
        });

        // re-enqueue the task if it should be retried
        // redis enque放在外面因为和postgres事务不在同一个事务里，避免postgre事务回滚导致redis也回滚
        if (Boolean.TRUE.equals(shouldRetry)) {
            taskQueueService.enqueue(taskId);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void recoverStaleTasks() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(STALE_TASK_SECONDS);

        List<ProcessingTask> staleTasks = taskRepository
                .findByStatusAndStartedAtBefore(TaskStatus.PROCESSING, cutoffTime);

        for (ProcessingTask task : staleTasks) {
            recoverStaleTask(task.getId());
        }
    }

    public void recoverStaleTask(Long taskId) {
        Boolean shouldRetry = transactionTemplate.execute(status -> {
            ProcessingTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            if (task.getStatus() != TaskStatus.PROCESSING) {
                return false;
            }

            int retryCount = task.getRetryCount() + 1;
            task.setRetryCount(retryCount);
            task.setErrorMessage("Worker timeout or crash detected");

            if (retryCount >= MAX_RETRIES) {
                task.setStatus(TaskStatus.FAILED);
                historyRepository.save(new TaskHistory(task, TaskStatus.FAILED));
                return false;
            }

            task.setStatus(TaskStatus.QUEUED);
            task.setStartedAt(null);
            historyRepository.save(new TaskHistory(task, TaskStatus.QUEUED));

            return true;
        });

        if (Boolean.TRUE.equals(shouldRetry)) {
            taskQueueService.enqueue(taskId);
        }
    }
}