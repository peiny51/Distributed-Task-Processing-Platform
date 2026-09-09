package com.taskplatform.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne  // Many TaskHistory entries can be associated with one ProcessingTask
    @JoinColumn(name = "task_id", nullable = false)
    private ProcessingTask task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TaskHistory() {
    }

    public TaskHistory(ProcessingTask task, TaskStatus status) {
        this.task = task;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public ProcessingTask getTask() {
        return task;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}