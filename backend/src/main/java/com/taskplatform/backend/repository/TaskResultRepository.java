package com.taskplatform.backend.repository;

import com.taskplatform.backend.entity.TaskResult;
import com.taskplatform.backend.entity.ProcessingTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskResultRepository extends JpaRepository<TaskResult, Long> {

    Optional<TaskResult> findByTask(ProcessingTask task);
}