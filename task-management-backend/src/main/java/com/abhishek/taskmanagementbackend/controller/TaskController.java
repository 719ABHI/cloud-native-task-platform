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
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller responsible for handling all HTTP requests
 * related to Task management.
 * It delegates business operations to the Service layer.
 */

@Tag(
        name = "Task Management",
        description = "APIs for managing tasks"
)
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
    @Operation(
            summary = "Create a new task",
            description = "Creates a task and stores it in the database."
    )
    @PostMapping
    public TaskResponse createTask(@Valid @RequestBody TaskRequest request) {
        return taskService.createTask(request);
    }

    /**
     * Retrieves a single task using its unique identifier.
     */
    @Operation(
            summary = "Get all tasks",
            description = "Returns a list of all available tasks."
    )

    @GetMapping
    public List<TaskResponse> getAllTasks() {
        return taskService.getAllTasks();
    }


    @Operation(
            summary = "Get task by ID",
            description = "Returns a task for the given ID."
    )
    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    /**
     * Updates an existing task identified by its ID.
     */
    @Operation(
            summary = "Update an existing task",
            description = "Updates the task details for the given ID."
    )
    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id,@Valid
                                   @RequestBody TaskRequest request){

        return taskService.updateTask(id, request);
    }

    /**
     * Deletes a task using its unique identifier.
     */
    @Operation(
            summary = "Delete a task",
            description = "Deletes the task with the given ID."
    )
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {

        taskService.deleteTask(id);

        return "Task deleted successfully";
    }
}