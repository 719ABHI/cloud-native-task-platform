package com.abhishek.taskmanagementbackend.dto;

/**
 * DTO returned after a successful login.
 * Currently contains only a success message.
 * JWT tokens will be added in a later story.
 */
public class LoginResponse {

    private String message;

    public LoginResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}