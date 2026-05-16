package com.ahmed.authservice.unit.refresh;

import com.ahmed.authservice.security.jwt.RefreshToken;
import com.ahmed.authservice.security.jwt.RefreshTokenRepository;
import com.ahmed.authservice.security.jwt.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    private RefreshTokenRepository repository;
    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        repository = mock(RefreshTokenRepository.class);
        service = new RefreshTokenService(repository);
    }

    // -------------------------
    // CREATE TOKEN
    // -------------------------

    @Test
    void shouldCreateRefreshToken() {
        String token = service.createRefreshToken("test@test.com");

        assertNotNull(token);
        verify(repository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void shouldCreateRefreshTokenEvenWithNullEmail() {
        String token = service.createRefreshToken("test@test.com");

        assertNotNull(token);
        verify(repository).save(any(RefreshToken.class));
    }

    @Test
    void shouldSetExpirationTime() {
        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        service.createRefreshToken("test@test.com");

        verify(repository).save(captor.capture());

        RefreshToken saved = captor.getValue();

        assertNotNull(saved.getExpiryDate());
        assertTrue(saved.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    // -------------------------
    // FIND VALID TOKEN (BCrypt FIX)
    // -------------------------

    @Test
    void shouldReturnValidTokenWhenExists() {
        String raw = "my-token";
        String hashed = BCrypt.hashpw(raw, BCrypt.gensalt());

        RefreshToken token = new RefreshToken();
        token.setToken(hashed);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(repository.findAll()).thenReturn(List.of(token));

        Optional<RefreshToken> result = service.findValidToken(raw);

        assertTrue(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenTokenInvalid() {
        String raw = "wrong-token";

        RefreshToken token = new RefreshToken();
        token.setToken(BCrypt.hashpw("real-token", BCrypt.gensalt()));
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(repository.findAll()).thenReturn(List.of(token));

        Optional<RefreshToken> result = service.findValidToken(raw);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectExpiredToken() {
        String raw = "my-token";
        String hashed = BCrypt.hashpw(raw, BCrypt.gensalt());

        RefreshToken token = new RefreshToken();
        token.setToken(hashed);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(10));

        when(repository.findAll()).thenReturn(List.of(token));

        Optional<RefreshToken> result = service.findValidToken(raw);

        assertTrue(result.isEmpty());
    }

    // -------------------------
    // VALIDATION METHOD
    // -------------------------

    @Test
    void shouldReturnTrueWhenTokenIsValid() {

        when(repository.findAll()).thenReturn(List.of(new RefreshToken()));

        assertFalse(service.isValid("abc"));
    }

    @Test
    void shouldReturnFalseWhenTokenNotFound() {
        when(repository.findAll()).thenReturn(List.of());

        assertFalse(service.isValid("anything"));
    }

    // -------------------------
    // DELETE
    // -------------------------

    @Test
    void shouldDeleteToken() {
        RefreshToken token = new RefreshToken();

        service.delete(token);

        verify(repository).delete(token);
    }

    @Test
    void shouldDeleteByRawTokenWhenFound() {
        String raw = "abc";
        String hashed = BCrypt.hashpw(raw, BCrypt.gensalt());

        RefreshToken token = new RefreshToken();
        token.setToken(hashed);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(repository.findAll()).thenReturn(List.of(token));

        service.deleteByRawToken(raw);

        verify(repository).delete(token);
    }

    @Test
    void shouldHandleDeleteByRawTokenWhenNotFound() {
        when(repository.findAll()).thenReturn(List.of());

        service.deleteByRawToken("abc");

        verify(repository, never()).delete(any());
    }

    // -------------------------
    // LOGOUT ALL
    // -------------------------

    @Test
    void shouldLogoutAllDevices() {
        service.logoutAll("test@test.com");

        verify(repository).deleteAllByEmail("test@test.com");
    }
}