package com.abhishek.taskmanagementbackend.exception;

/**
 * Thrown when the provided login credentials
 * are invalid.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }



}