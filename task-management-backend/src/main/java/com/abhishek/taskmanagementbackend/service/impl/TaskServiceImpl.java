package com.abhishek.taskmanagementbackend.service.impl;

import com.abhishek.taskmanagementbackend.entity.Task;
import com.abhishek.taskmanagementbackend.repository.TaskRepository;
import com.abhishek.taskmanagementbackend.service.TaskService;
import org.springframework.stereotype.Service;
import com.abhishek.taskmanagementbackend.dto.TaskRequest;
import com.abhishek.taskmanagementbackend.dto.TaskResponse;

import java.util.ArrayList;
import java.util.List;


/**
 * Implements business logic for Task operations.
 * Responsible for converting DTOs to Entities,
 * interacting with the Repository,
 * and returning appropriate responses.
 */
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    // Constructor Injection for TaskRepository.
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Converts the client request DTO into a Task entity.
     */
    private Task mapToEntity(TaskRequest request) {

        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return task;
    }

    /**
     * Converts a Task entity into a response DTO.
     */
    private TaskResponse mapToResponse(Task task) {

        TaskResponse response = new TaskResponse();

        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        return response;
    }

    /**
     * Creates a new task.

     * Flow:
     * TaskRequest DTO
     *      ↓
     * Task Entity
     *      ↓
     * Save into Database
     *      ↓
     * TaskResponse DTO
     */
    @Override
    public TaskResponse createTask(TaskRequest request) {

        Task task = mapToEntity(request);

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask);
    }

    /**
     * Retrieves all tasks from the database and converts
     * each Task entity into a TaskResponse DTO.
     */
    @Override
    public List<TaskResponse> getAllTasks() {

        List<Task> tasks = taskRepository.findAll();

        List<TaskResponse> responses = new ArrayList<>();

        for (Task task : tasks) {
            responses.add(mapToResponse(task));
        }

        return responses;
    }

    /**
     * Retrieves a task by its ID and converts
     * the entity into a TaskResponse DTO.
     */

    @Override
    public TaskResponse getTaskById(Long id) {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow();

        return mapToResponse(existingTask);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow();

        existingTask.setTitle(request.getTitle());
        existingTask.setDescription(request.getDescription());
        existingTask.setStatus(request.getStatus());
        existingTask.setPriority(request.getPriority());
        existingTask.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(existingTask);

        return mapToResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow();

        taskRepository.delete(existingTask);
    }
}