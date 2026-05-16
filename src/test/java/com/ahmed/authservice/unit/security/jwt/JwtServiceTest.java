package com.ahmed.authservice.unit.security.jwt;

import com.ahmed.authservice.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "01234567890123456789012345678901", // 32+ chars secret
                3600000
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {

        String token = jwtService.generateToken("test@test.com", "USER");

        assertNotNull(token);
        assertTrue(token.contains("."));

        UserDetails user = User.withUsername("test@test.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void shouldInvalidateTokenForDifferentUser() {

        String token = jwtService.generateToken("test@test.com", "USER");

        UserDetails differentUser = User.withUsername("other@test.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void shouldExtractUsernameFromToken() {

        String email = "test@test.com";
        String token = jwtService.generateToken(email, "USER");

        assertEquals(email, jwtService.extractUsername(token));
    }

    @Test
    void shouldExtractRoleFromToken() {

        String token = jwtService.generateToken("test@test.com", "ADMIN");

        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    void shouldRejectExpiredToken() throws InterruptedException {

        JwtService shortJwt = new JwtService(
                "01234567890123456789012345678901",
                10
        );

        String token = shortJwt.generateToken("test@test.com", "USER");

        Thread.sleep(20);

        UserDetails user = User.withUsername("test@test.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        assertFalse(shortJwt.isTokenValid(token, user));
    }

    @Test
    void shouldRejectTamperedToken() {

        String token = jwtService.generateToken("test@test.com", "USER");

        String tampered = token + "abc";

        UserDetails user = User.withUsername("test@test.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        assertFalse(jwtService.isTokenValid(tampered, user));
    }

    @Test
    void shouldRejectNullOrEmptyToken() {

        UserDetails user = User.withUsername("test@test.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        assertFalse(jwtService.isTokenValid(null, user));
        assertFalse(jwtService.isTokenValid("", user));
    }
}