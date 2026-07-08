package com.abhishek.taskmanagementbackend.controller;

import com.abhishek.taskmanagementbackend.service.TaskService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abhishek.taskmanagementbackend.entity.Task;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.DeleteMapping;
import com.abhishek.taskmanagementbackend.dto.TaskRequest;
import com.abhishek.taskmanagementbackend.dto.TaskResponse;

/**
 * REST Controller responsible for handling all HTTP requests
 * related to Task management.
 * It delegates business operations to the Service layer.
 */

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    // Constructor Injection ensures TaskService is provided by Spring.
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Creates a new task.
     * Accepts a TaskRequest DTO(Data Transfer Object) from the client and
     * returns a TaskResponse DTO after successful creation.
     */
    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request) {
        return taskService.createTask(request);
    }

    /**
     * Retrieves a single task using its unique identifier.
     */
    @GetMapping
    public List<TaskResponse> getAllTasks() {
        return taskService.getAllTasks();
    }


    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    /**
     * Updates an existing task identified by its ID.
     */
    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id,
                                   @RequestBody TaskRequest request){

        return taskService.updateTask(id, request);
    }

    /**
     * Deletes a task using its unique identifier.
     */
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {

        taskService.deleteTask(id);

        return "Task deleted successfully";
    }
}