package com.ahmed.authservice.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * NOTE:
 * BCrypt hashing is intentionally used for refresh token storage security.

 * Since BCrypt is non-deterministic, token lookup currently scans stored tokens.
 * This is acceptable for demo-scale applications.

 * Production systems should use:
 * - Redis-backed session storage
 * - Deterministic hashing (SHA-256)
 * - Token identifiers (tokenId + hash strategy)
 **/


/**
 * Manages refresh tokens for session persistence.

 * Responsibilities:
 * - Generate secure refresh tokens
 * - Store hashed tokens
 * - Validate tokens
 * - Handle logout (single / all devices)
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    /**
     * Creates a new refresh token.

     * Token is:
     * - randomly generated (UUID)
     * - hashed before storage (security best practice)
     *
     * @return raw token (sent to client)
     */
    public String createRefreshToken(String email) {

        String rawToken = UUID.randomUUID().toString();
        // Store hashed version for security
        String hashedToken = BCrypt.hashpw(rawToken, BCrypt.gensalt());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(hashedToken);
        refreshToken.setEmail(email);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        repository.save(refreshToken);

        return rawToken;
    }

    public boolean isValid(String token) {
        return repository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }
    /**
     * find all refresh tokens for a user.
     */
    public Optional<RefreshToken> findValidToken(String rawToken) {
        return repository.findAll().stream()
                .filter(t -> BCrypt.checkpw(rawToken, t.getToken()))
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .findFirst();
    }

    public void delete(RefreshToken token) {
        repository.delete(token);
    }

    public void deleteByRawToken(String rawToken) {
        findValidToken(rawToken).ifPresent(repository::delete);
    }

    /**
     * Deletes all refresh tokens for a user (logout from all devices).
     */
    public void logoutAll(String email) {
        repository.deleteAllByEmail(email);
    }
}