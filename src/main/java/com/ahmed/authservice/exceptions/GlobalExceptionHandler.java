package com.ahmed.authservice.exceptions;

import com.ahmed.authservice.registration.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

/**
 * Centralized exception handling for the application.

 * Converts exceptions into consistent API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ApiError(ex.getMessage(), 400));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .internalServerError()
                .body(new ApiError("Internal server error", 500));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(401)
                .body(new ApiError("Invalid email or password", 401, LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity
                .badRequest()
                .body(new ApiError(errorMessage, 400));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiError> handleLocked(AccountLockedException ex) {
        return ResponseEntity.status(423)
                .body(new ApiError(ex.getMessage(), 423));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiError> handleTokenExpired(TokenExpiredException ex) {
        return ResponseEntity.status(401)
                .body(new ApiError(ex.getMessage(), 401));
    }
}