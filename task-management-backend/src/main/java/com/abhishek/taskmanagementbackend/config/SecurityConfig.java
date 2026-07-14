package com.abhishek.taskmanagementbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.abhishek.taskmanagementbackend.security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Configures Spring Security for the application.

 * Currently, all endpoints are publicly accessible.
 * Authentication and authorization rules will be added
 * incrementally in the upcoming stories of Sprint 3.
 */
@Configuration
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    /**
     * Creates a BCrypt PasswordEncoder bean used
     * to securely hash user passwords before storing them.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF because this project exposes REST APIs.
                // We'll use JWT instead of session-based authentication.
                .csrf(csrf -> csrf.disable())

                // Configure endpoint authorization rules.
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints.
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Every other endpoint requires authentication.
                        .anyRequest().authenticated()
                )

                // Register our JWT filter before Spring Security's
                // default username/password authentication filter.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .httpBasic(Customizer.withDefaults());
        return http.build();

    }
}