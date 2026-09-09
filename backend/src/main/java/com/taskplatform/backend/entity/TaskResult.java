package com.taskplatform.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_results")
public class TaskResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique identifier for the task, should match the id of the ProcessingTask
    // (Consistency)
    @OneToOne // 一个 TaskResult 对应一个 ProcessingTask。一个任务最多一个最终 result
    @JoinColumn(name = "task_id", nullable = false, unique = true) // 链接到 ProcessingTask 的 id
    private ProcessingTask task;

    @Column(name = "output_path", nullable = false)
    private String outputPath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TaskResult() {
    }

    public TaskResult(ProcessingTask task, String outputPath) {
        this.task = task;
        this.outputPath = outputPath;
    }

    public Long getId() {
        return id;
    }

    public ProcessingTask getTask() {
        return task;
    }

    public void setTask(ProcessingTask task) {
        this.task = task;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}