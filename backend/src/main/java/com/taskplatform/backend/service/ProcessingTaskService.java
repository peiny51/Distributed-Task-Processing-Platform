package com.taskplatform.backend.service;

import com.taskplatform.backend.entity.ProcessingTask;
import com.taskplatform.backend.entity.TaskHistory;
import com.taskplatform.backend.entity.TaskResult;
import com.taskplatform.backend.entity.TaskStatus;
import com.taskplatform.backend.entity.TaskType;
import com.taskplatform.backend.repository.ProcessingTaskRepository;
import com.taskplatform.backend.repository.TaskHistoryRepository;
import com.taskplatform.backend.repository.TaskResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcessingTaskService {

    private final ProcessingTaskRepository taskRepository;
    private final TaskResultRepository resultRepository;
    private final TaskHistoryRepository historyRepository;
    private final TaskCreationService taskCreationService;

    public ProcessingTaskService(ProcessingTaskRepository taskRepository, TaskHistoryRepository historyRepository,
            TaskResultRepository resultRepository, TaskCreationService taskCreationService) {
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.resultRepository = resultRepository;
        this.taskCreationService = taskCreationService;
    }

    // 先查这个 key
    // ↓
    // 存在？
    // ├─ Yes → 返回旧 task
    // └─ No → 创建新 task
    public ProcessingTask createTask(TaskType taskType, String inputPath, String idempotencyKey) {
        Optional<ProcessingTask> existingTask = taskRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTask.isPresent()) {
            return existingTask.get();
        }

        try {
            return taskCreationService.createNewTask(taskType, inputPath, idempotencyKey);
        } catch (DataIntegrityViolationException e) {
            return taskRepository.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> e);
        }
    }

    public List<ProcessingTask> getAllTasks() {
        return taskRepository.findAll();
    }

    public ProcessingTask getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Transactional
    public ProcessingTask completeTask(long taskId, String outputPath) {
        ProcessingTask task = getTaskById(taskId);

        task.setStatus(TaskStatus.COMPLETED);
        task.setProgress(100);
        task.setCompletedAt(LocalDateTime.now());

        taskRepository.save(task);

        TaskResult result = new TaskResult(task, outputPath);
        resultRepository.save(result);

        TaskHistory history = new TaskHistory(task, TaskStatus.COMPLETED);
        // throw new RuntimeException("Simulated failure");
        historyRepository.save(history);

        return task;
    }

    @Transactional
    public ProcessingTask claimNextTask() {
        ProcessingTask task = taskRepository.findNextQueuedTaskForUpdate()
                .orElseThrow(() -> new RuntimeException("No queued task available"));

        task.setStatus(TaskStatus.PROCESSING);
        task.setStartedAt(LocalDateTime.now());

        return task;
        // return taskRepository.save(task);
        // //自动保存更改，因为在@Transactional注解下，实体的状态变化会被自动检测并保存到数据库中。
    }

    public List<TaskHistory> getTaskHistory(Long taskId) {
        ProcessingTask task = getTaskById(taskId);
        return historyRepository.findByTaskOrderByCreatedAtAsc(task);
    }

    public TaskResult getTaskResult(Long taskId) {
        ProcessingTask task = getTaskById(taskId);
        return resultRepository.findByTask(task).orElseThrow(() -> new RuntimeException("Task result not found"));
    }
}