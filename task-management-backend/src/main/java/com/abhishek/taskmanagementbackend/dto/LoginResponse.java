package com.abhishek.taskmanagementbackend.dto;

/**
 * DTO returned after a successful authentication.
 * Contains the JWT access token that the client
 * must send with future requests.
 */
public class LoginResponse {

    private String accessToken;
    private String tokenType;

    public LoginResponse() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}