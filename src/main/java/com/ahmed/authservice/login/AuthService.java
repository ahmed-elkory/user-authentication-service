package com.ahmed.authservice.login;

import com.ahmed.authservice.appuser.AppUser;
import com.ahmed.authservice.appuser.AppUserRepository;
import com.ahmed.authservice.exceptions.AccountLockedException;
import com.ahmed.authservice.security.jwt.JwtService;
import com.ahmed.authservice.security.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles authentication logic including login,
 * account locking, and token generation.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final RefreshTokenService refreshTokenService;
    @Value("${app.lock.duration}")
    private int lockDurationMinutes;


    /**
     * Authenticates user and generates access + refresh tokens.

     * Flow:
     * 1. Validate user existence
     * 2. Check email verification
     * 3. Handle account locking
     * 4. Authenticate credentials
     * 5. Reset failed attempts
     * 6. Generate tokens
     *
     * @param request login request (email + password)
     * @param ip client IP address
     * @param userAgent client device info
     * @return authentication response (tokens)
     */

    @Transactional
    public AuthResponse login(LoginRequest request , String ip, String userAgent) {

        // Log login attempt for auditing
        log.info("LOGIN ATTEMPT | email={} | ip={} | agent={}",
                request.email(), ip, userAgent);

        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Ensure email is verified
        if (!user.isEnabled()) {
            throw new IllegalStateException("Please verify your email first");
        }

        // Handle lock/unlock logic
        handleAccountLock(user);

        try {
            // Authenticate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            // Reset failed attempts on success
            user.setFailedAttempts(0);
            user.setLastLogin(LocalDateTime.now());

            appUserRepository.save(user);

            log.info("LOGIN SUCCESS | email={} | ip={}",
                    user.getEmail(), ip);

            // Generate JWT access token
            String accessToken = jwtService.generateToken(
                    user.getUsername(),
                    user.getAppUserRole().name()
            );

            // Generate refresh token
            String refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

            return new AuthResponse(accessToken, refreshToken);

        } catch (BadCredentialsException e) {

            // Handle failed login attempts
            handleFailedLogin(user);

            throw new BadCredentialsException("Invalid email or password");
        }
    }


    /**
     * Handles account locking logic.
     * Unlocks account if lock duration has expired.
     */
    private void handleAccountLock(AppUser user) {

        if (user.isLocked()) {

            if (user.getLockTime() != null &&
                    user.getLockTime().plusMinutes(lockDurationMinutes).isBefore(LocalDateTime.now())) {
                // Unlock account
                user.setLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);

                appUserRepository.save(user);

                log.info("ACCOUNT UNLOCKED | email={}", user.getEmail());

            } else {
                throw new AccountLockedException("Account is locked. Try again later.");
            }
        }
    }

    /**
     * Increments failed login attempts and locks account if threshold is reached.
     */
    private void handleFailedLogin(AppUser user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= 5) {
            user.setLocked(true);
            user.setLockTime(LocalDateTime.now());

            log.warn("LOGIN FAILED | email={} | attempts={}",
                    user.getEmail(),
                    user.getFailedAttempts());
        }

        appUserRepository.save(user);
    }
}