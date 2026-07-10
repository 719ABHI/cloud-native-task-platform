package com.abhishek.taskmanagementbackend.service;

import com.abhishek.taskmanagementbackend.dto.LoginRequest;
import com.abhishek.taskmanagementbackend.dto.LoginResponse;
import com.abhishek.taskmanagementbackend.dto.RegisterRequest;
import com.abhishek.taskmanagementbackend.dto.UserResponse;

/**
 * Defines business operations related to user management.
 */
public interface UserService {

    /**
     * Registers a new user in the system.
     */
    UserResponse registerUser(RegisterRequest request);

    /**
     * Authenticates a user using email and password.
     */
    LoginResponse loginUser(LoginRequest request);
}