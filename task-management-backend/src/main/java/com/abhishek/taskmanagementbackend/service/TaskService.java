package com.abhishek.taskmanagementbackend.service;
import java.util.List;
import com.abhishek.taskmanagementbackend.entity.Task;
import com.abhishek.taskmanagementbackend.dto.TaskRequest;
import com.abhishek.taskmanagementbackend.dto.TaskResponse;


/**
 * Defines business operations related to Task management.
 * The Service layer acts as a bridge between
 * the Controller and Repository.
 */
public interface TaskService {

   // Task createTask(Task task);
   TaskResponse createTask(TaskRequest request);

    List<TaskResponse> getAllTasks();

    TaskResponse getTaskById(Long id);

    TaskResponse updateTask(Long id, TaskRequest request);

    void deleteTask(Long id);

}