package com.abhishek.taskmanagementbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures Spring Security for the application.

 * Currently, all endpoints are publicly accessible.
 * Authentication and authorization rules will be added
 * incrementally in the upcoming stories of Sprint 3.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        /**
         * Creates a BCrypt PasswordEncoder bean used
         * to securely hash user passwords before storing them.
         */


        http
                // Disable CSRF because this project exposes REST APIs.
                // We'll use JWT instead of session-based authentication.
                .csrf(csrf -> csrf.disable())

                // Allow every request for now.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // Use Spring Security's default configuration.
                .httpBasic(Customizer.withDefaults());


        return http.build();

    }
}