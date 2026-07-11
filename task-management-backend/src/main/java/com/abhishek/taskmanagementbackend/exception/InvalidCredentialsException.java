package com.abhishek.taskmanagementbackend.exception;
import com.abhishek.taskmanagementbackend.exception.InvalidCredentialsException;

/**
 * Thrown when the provided login credentials
 * are invalid.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

}