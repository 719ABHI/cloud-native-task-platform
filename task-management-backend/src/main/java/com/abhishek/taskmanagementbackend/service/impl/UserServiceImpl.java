package com.abhishek.taskmanagementbackend.service.impl;

import com.abhishek.taskmanagementbackend.dto.RegisterRequest;
import com.abhishek.taskmanagementbackend.dto.UserResponse;
import com.abhishek.taskmanagementbackend.entity.Role;
import com.abhishek.taskmanagementbackend.entity.User;
import com.abhishek.taskmanagementbackend.repository.UserRepository;
import com.abhishek.taskmanagementbackend.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.abhishek.taskmanagementbackend.dto.LoginRequest;
import com.abhishek.taskmanagementbackend.dto.LoginResponse;
import com.abhishek.taskmanagementbackend.exception.InvalidCredentialsException;

/**
 * Implements business logic related to user management.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Converts the incoming registration request
     * into a User entity before persisting it.
     */
    private User mapToEntity(RegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Store the BCrypt hash instead of the plain-text password.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Every newly registered user gets the USER role.
        user.setRole(Role.USER);

        return user;
    }
    /**
     * Converts a User entity into a UserResponse DTO
     * to avoid exposing sensitive information.
     */
    private UserResponse mapToResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    @Override
    public UserResponse registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = mapToEntity(request);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    /**
     * Authenticates a user by verifying the email
     * and matching the provided password.
     */
    @Override
    public LoginResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        LoginResponse response = new LoginResponse();
        response.setMessage("Login Successful");

        return response;
    }
}