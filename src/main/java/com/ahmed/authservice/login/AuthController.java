package com.ahmed.authservice.login;

import com.ahmed.authservice.common.ApiResponse;
import com.ahmed.authservice.security.jwt.JwtService;
import com.ahmed.authservice.security.jwt.RefreshTokenService;
import com.ahmed.authservice.security.rate_limit.RateLimiterService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for authentication-related endpoints.

 * Provides APIs for:
 * - User login
 * - Logout (single device)
 * - Logout from all devices

 * Includes rate limiting to protect against brute-force attacks.
 */
@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimiterService rateLimiterService;
    private final JwtService jwtService;



    /**
     * Authenticates user credentials and returns JWT tokens.

     * Applies rate limiting per IP address to prevent abuse.
     *
     * @param request HTTP request (used to extract IP & User-Agent)
     * @param req login request payload
     * @return access + refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            HttpServletRequest request,
            @Valid @RequestBody LoginRequest req
    ) {

        // Extract client metadata for logging & rate limiting
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Apply rate limiting
        Bucket bucket = rateLimiterService.resolveBucket(ip);

        if (!bucket.tryConsume(1)) {
            throw new IllegalStateException("Too many login attempts. Try again later.");
        }

        AuthResponse response = authService.login(req, ip, userAgent);

        return ResponseEntity.ok(new ApiResponse<>("Login successful", response));
    }

    /**
     * Logs out a user by invalidating a specific refresh token.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.deleteByRawToken(request.refreshToken());
        return ResponseEntity.ok(
                new ApiResponse<>("Logged out successfully", null)
        );
    }

    /**
     * Logs out user from all devices by removing all refresh tokens.
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Missing token");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        refreshTokenService.logoutAll(email);

        return ResponseEntity.ok(new ApiResponse<>("Logged out from all devices", null));
    }
}