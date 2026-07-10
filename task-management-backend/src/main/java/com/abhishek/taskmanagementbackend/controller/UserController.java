package com.abhishek.taskmanagementbackend.controller;

import com.abhishek.taskmanagementbackend.dto.LoginRequest;
import com.abhishek.taskmanagementbackend.dto.LoginResponse;
import com.abhishek.taskmanagementbackend.dto.RegisterRequest;
import com.abhishek.taskmanagementbackend.dto.UserResponse;
import com.abhishek.taskmanagementbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes REST endpoints for user authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user.
     */
    @PostMapping("/register")
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    /**
     * Authenticates an existing user.
     */
    @PostMapping("/login")
    public LoginResponse loginUser(@Valid @RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }
}