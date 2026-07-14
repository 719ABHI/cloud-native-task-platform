package com.abhishek.taskmanagementbackend.service;
/**
 * Defines operations related to JSON Web Token (JWT)
 * generation and validation.
 */
public interface JwtService {

    /**
     * Generates a JWT for the specified user.
     */
    String generateToken(String email);

    /**
     * Extracts the user's email (subject)
     * from the JWT.
     */
    String extractUsername(String token);

    /**
     * Checks whether the JWT is valid.
     */
    boolean isTokenValid(String token);
}