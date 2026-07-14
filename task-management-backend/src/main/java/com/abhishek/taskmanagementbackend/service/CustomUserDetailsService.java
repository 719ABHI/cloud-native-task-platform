package com.abhishek.taskmanagementbackend.service;

import com.abhishek.taskmanagementbackend.entity.User;
import com.abhishek.taskmanagementbackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details from the database for Spring Security.
 * Spring Security calls this service whenever it needs to
 * authenticate a user.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

   /* Spring Security uses the term: username
    to mean :"The unique user identifier." */
   @Override
   public UserDetails loadUserByUsername(String username)
           throws UsernameNotFoundException {

       User user = userRepository.findByEmail(username)
               .orElseThrow(() ->
                       new UsernameNotFoundException(
                               "User not found with email: " + username));

       return org.springframework.security.core.userdetails.User
               .withUsername(user.getEmail())
               .password(user.getPassword())
               .roles(user.getRole().name())
               .build();
   }
}