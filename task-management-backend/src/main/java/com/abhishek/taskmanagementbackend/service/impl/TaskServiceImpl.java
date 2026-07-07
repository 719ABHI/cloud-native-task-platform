package com.abhishek.taskmanagementbackend.service.impl;

import com.abhishek.taskmanagementbackend.entity.Task;
import com.abhishek.taskmanagementbackend.repository.TaskRepository;
import com.abhishek.taskmanagementbackend.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public Task updateTask(Long id, Task task) {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow();

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setPriority(task.getPriority());
        existingTask.setDueDate(task.getDueDate());

        return taskRepository.save(existingTask);
    }

    @Override
    public void deleteTask(Long id) {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow();

        taskRepository.delete(existingTask);
    }
}