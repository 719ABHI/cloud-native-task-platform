package com.abhishek.taskmanagementbackend.dto;

import com.abhishek.taskmanagementbackend.entity.TaskPriority;
import com.abhishek.taskmanagementbackend.entity.TaskStatus;

import java.time.LocalDate;

public class TaskRequest {

    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;

    public TaskRequest() {
    }

    public TaskRequest(String title,
                       String description,
                       TaskStatus status,
                       TaskPriority priority,
                       LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}