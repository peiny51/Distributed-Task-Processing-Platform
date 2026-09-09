package com.taskplatform.backend.repository;

import com.taskplatform.backend.entity.ProcessingTask;
import com.taskplatform.backend.entity.TaskStatus;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

public interface ProcessingTaskRepository extends JpaRepository<ProcessingTask, Long> {


//     SELECT *
// FROM processing_tasks
// WHERE idempotency_key = ?

// SELECT *
// FROM processing_tasks
// WHERE status = ?
//   AND started_at < ?;

    // @query: 告诉 Spring：这个 Repository 方法不要按方法名自动推导查询，而是使用我明确写的查询语句。
    @Query(value = """
        SELECT *
        FROM processing_tasks
        WHERE status = 'QUEUED'
        ORDER BY created_at ASC
        FOR UPDATE SKIP LOCKED
        LIMIT 1
        """, nativeQuery = true)
    Optional<ProcessingTask> findNextQueuedTaskForUpdate();
    Optional<ProcessingTask> findByIdempotencyKey(String idempotencyKey);
    List<ProcessingTask> findByStatusAndStartedAtBefore(TaskStatus status, LocalDateTime cutoffTime);
}