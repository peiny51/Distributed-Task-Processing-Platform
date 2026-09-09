package com.taskplatform.backend.service;

import com.taskplatform.backend.entity.OutboxMessage;
import com.taskplatform.backend.entity.ProcessingTask;
import com.taskplatform.backend.entity.TaskHistory;
import com.taskplatform.backend.entity.TaskStatus;
import com.taskplatform.backend.entity.TaskType;
import com.taskplatform.backend.repository.OutboxMessageRepository;
import com.taskplatform.backend.repository.ProcessingTaskRepository;
import com.taskplatform.backend.repository.TaskHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCreationService {

    private final ProcessingTaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;
    private final OutboxMessageRepository outboxRepository;

    public TaskCreationService(ProcessingTaskRepository taskRepository, TaskHistoryRepository historyRepository,
            OutboxMessageRepository outboxRepository) {
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
public ProcessingTask createNewTask(TaskType taskType, String inputPath, String idempotencyKey) {
    ProcessingTask task = new ProcessingTask();
    task.setTaskType(taskType);
    task.setStatus(TaskStatus.QUEUED);
    task.setProgress(0);
    task.setInputPath(inputPath);
    task.setIdempotencyKey(idempotencyKey);

    ProcessingTask savedTask = taskRepository.saveAndFlush(task);

    historyRepository.save(new TaskHistory(savedTask, TaskStatus.QUEUED));

    OutboxMessage message = new OutboxMessage("TASK_CREATED", savedTask.getId(), savedTask.getId().toString());
    outboxRepository.save(message);

    return savedTask;
}
}