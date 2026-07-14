package com.abhishek.taskmanagementbackend.security;

import com.abhishek.taskmanagementbackend.service.CustomUserDetailsService;
import com.abhishek.taskmanagementbackend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every incoming HTTP request to
 * extract and validate the JWT before the
 * request reaches the controller.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // =========================
    // Dependencies
    // =========================
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Injects the services required to validate JWTs
     * and load authenticated users.
     */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Read the Authorization header from the incoming request.
        String authHeader = request.getHeader("Authorization");

        // If the Authorization header is missing or does not contain
        // a Bearer token, continue the filter chain.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Remove the "Bearer " prefix to obtain only the JWT.
        String jwt = authHeader.substring(7);

        // Extract the user's email from the JWT.
        String email = jwtService.extractUsername(jwt);

        // Load the authenticated user's details from the database.
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        // We will validate the token and authenticate the user
        // in the next step.
        if (jwtService.isTokenValid(jwt)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        // Continue processing the request after JWT validation.
        filterChain.doFilter(request, response);
    }
}