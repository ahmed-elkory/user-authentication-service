package com.ahmed.authservice.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object used to handle user login requests.

 * Contains only the necessary credentials required for authentication:
 * email and password.

 * This class is used to decouple the API layer from the persistence layer
 * and prevent exposing internal user entity details.
 */
public record LoginRequest(

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8)
        String password
) {}