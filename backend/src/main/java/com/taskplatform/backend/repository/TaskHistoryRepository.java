package com.taskplatform.backend.repository;

import com.taskplatform.backend.entity.ProcessingTask;
import com.taskplatform.backend.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskOrderByCreatedAtAsc(ProcessingTask task);
}