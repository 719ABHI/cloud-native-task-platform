package com.abhishek.taskmanagementbackend.service.impl;

import com.abhishek.taskmanagementbackend.entity.Task;
import com.abhishek.taskmanagementbackend.repository.TaskRepository;
import com.abhishek.taskmanagementbackend.service.TaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
}