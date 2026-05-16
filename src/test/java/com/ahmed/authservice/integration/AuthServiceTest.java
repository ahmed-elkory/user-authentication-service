package com.ahmed.authservice.integration;

import com.ahmed.authservice.appuser.AppUser;
import com.ahmed.authservice.appuser.AppUserRepository;
import com.ahmed.authservice.appuser.AppUserRole;
import com.ahmed.authservice.exceptions.AccountLockedException;
import com.ahmed.authservice.login.AuthResponse;
import com.ahmed.authservice.login.AuthService;
import com.ahmed.authservice.login.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AppUserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private final String EMAIL = "admin@test.com";
    private final String PASSWORD = "admin123";

    @BeforeEach
    void setup() {
        repository.deleteAll();

        AppUser user = new AppUser(
                "Ahmed",
                "Test",
                EMAIL,
                passwordEncoder.encode(PASSWORD),
                AppUserRole.USER
        );

        user.setEnabled(true);
        repository.save(user);
    }

    // -------------------------
    // 1. SUCCESSFUL LOGIN
    // -------------------------
    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        AuthResponse response =
                authService.login(request, "127.0.0.1", "JUnit");

        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
    }

    // -------------------------
    // 2. WRONG PASSWORD
    // -------------------------
    @Test
    void shouldFailWithWrongPassword() {

        LoginRequest request = new LoginRequest(EMAIL, "wrong-password");

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request, "127.0.0.1", "JUnit")
        );
    }


    // -------------------------
    // 4. LOCKED ACCOUNT CANNOT LOGIN
    // -------------------------
    @Test
    void shouldNotAllowLoginWhenAccountIsLocked() {

        AppUser user = repository.findByEmail(EMAIL).orElseThrow();

        user.setLocked(true);
        repository.save(user);

        LoginRequest request =
                new LoginRequest(EMAIL, PASSWORD);

        assertThrows(
                AccountLockedException.class,
                () -> authService.login(request, "127.0.0.1", "JUnit")
        );
    }

    // -------------------------
    // 5. SUCCESSFUL LOGIN RESETS FAILED ATTEMPTS
    // -------------------------
    @Test
    void shouldResetFailedAttemptsOnSuccessfulLogin() {

        // First: cause failure
        try {
            authService.login(
                    new LoginRequest(EMAIL, "wrong"),
                    "127.0.0.1",
                    "JUnit"
            );
        } catch (Exception ignored) {}

        // Then successful login
        authService.login(
                new LoginRequest(EMAIL, PASSWORD),
                "127.0.0.1",
                "JUnit"
        );

        AppUser user =
                repository.findByEmail(EMAIL).orElseThrow();

        assertEquals(0, user.getFailedAttempts());
    }

    // -------------------------
    // 6. DISABLED USER CANNOT LOGIN
    // -------------------------
    @Test
    void shouldRejectDisabledUser() {

        AppUser user = repository.findByEmail(EMAIL).orElseThrow();
        user.setEnabled(false);
        repository.save(user);

        LoginRequest request =
                new LoginRequest(EMAIL, PASSWORD);

        assertThrows(
                IllegalStateException.class,
                () -> authService.login(request, "127.0.0.1", "JUnit")
        );
    }
}