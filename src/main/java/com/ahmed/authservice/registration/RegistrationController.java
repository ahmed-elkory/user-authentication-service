package com.ahmed.authservice.registration;

import com.ahmed.authservice.appuser.AppUserRepository;
import com.ahmed.authservice.common.ApiResponse;
import com.ahmed.authservice.exceptions.TokenExpiredException;
import com.ahmed.authservice.login.AuthResponse;
import com.ahmed.authservice.login.RefreshTokenRequest;
import com.ahmed.authservice.security.jwt.JwtService;
import com.ahmed.authservice.security.jwt.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * REST controller responsible for user registration and email verification.

 * Exposes endpoints for:
 * - User registration
 * - Email confirmation via token
 * - Token refresh for expired verification links

 * Acts as the entry point for the onboarding flow.
 */
@RestController
@RequestMapping( "${api.prefix}/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    /**
     * Registers a new user account.

     * Delegates creation and email sending to RegistrationService.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegistrationRequest request) {
        registrationService.register(request);
        return ResponseEntity.ok(
                new ApiResponse<>("User registered successfully", null)
        );
    }

    /**
     * Confirms user email using a verification token.

     * Activates the user account if token is valid.
     */
    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(
            @RequestParam(value = "token", required = false) String token
    ) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required for email confirmation");
        }

        registrationService.confirmToken(token);

        return ResponseEntity.ok(
                new ApiResponse<>("Email confirmed successfully", null)
        );
    }

    /**
     * Issues a new access token using a valid refresh token.

     * Used when access token expires but session is still valid.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody @Valid RefreshTokenRequest request) {

        var token = refreshTokenService.findValidToken(request.refreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.delete(token);

        var user = appUserRepository.findByEmail(token.getEmail())
                .orElseThrow();

        String newAccessToken = jwtService.generateToken(
                user.getEmail(),
                user.getAppUserRole().name()
        );

        String newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Token refreshed successfully",
                        new AuthResponse(newAccessToken, newRefreshToken)
                )
        );
    }
}
