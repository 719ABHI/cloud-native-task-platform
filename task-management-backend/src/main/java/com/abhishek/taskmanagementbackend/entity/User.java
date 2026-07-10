package com.abhishek.taskmanagementbackend.entity;

import jakarta.persistence.*;

/**
 * Represents an application user who can authenticate
 * and access the Task Management Platform.
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username used to identify the user.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email address used for authentication and communication.
     * Each email must be unique across all users.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Stores the BCrypt hashed password.
     * Plain-text passwords must never be stored in the database.
     */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    public User() { }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Defines the user's role within the application.
     * Used later for role-based authorization.
     */


}