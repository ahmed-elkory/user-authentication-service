package com.ahmed.authservice.security.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByExpiryDateBefore(LocalDateTime time);

    void deleteAllByEmail(String email);
}