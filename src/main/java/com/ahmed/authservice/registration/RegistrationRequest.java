package com.ahmed.authservice.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object used for user registration requests.

 * Carries the necessary information required to create a new user account:
 * - First name
 * - Last name
 * - Email
 * - Password

 * This DTO is used to decouple the API layer from the persistence layer
 * and to enforce validation rules before processing registration logic.
 */
public record RegistrationRequest(

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
                message = "Password must be at least 8 characters and include uppercase, lowercase, number and special character"
        )
        String password
) {}