package com.ahmed.authservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security configuration for password encoding.

 * Provides a BCryptPasswordEncoder bean used by Spring Security
 * to hash and verify user passwords securely.

 * BCrypt is a strong adaptive hashing algorithm that protects
 * against brute-force and rainbow table attacks.
 */
@Configuration
public class PasswordConfig {

    /**
     * Defines the password encoder used across the application.

     * @return BCryptPasswordEncoder instance for hashing passwords
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
