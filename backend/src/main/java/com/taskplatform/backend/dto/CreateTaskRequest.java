package com.taskplatform.backend.dto;

import com.taskplatform.backend.entity.TaskType;
import jakarta.validation.constraints.NotNull;

public class CreateTaskRequest {

    @NotNull
    private TaskType taskType;

    private String inputPath;

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }
}