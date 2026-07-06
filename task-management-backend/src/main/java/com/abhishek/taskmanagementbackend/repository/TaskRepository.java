package com.abhishek.taskmanagementbackend.repository;

import com.abhishek.taskmanagementbackend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

}