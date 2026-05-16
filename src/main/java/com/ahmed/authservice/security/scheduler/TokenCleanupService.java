package com.ahmed.authservice.security.scheduler;

import com.ahmed.authservice.registration.token.ConfirmationTokenRepository;
import com.ahmed.authservice.security.jwt.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled job for cleaning expired tokens.

 * Prevents database bloat by periodically removing:
 * - expired confirmation tokens
 * - expired refresh tokens
 */
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Runs every hour.
     */
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // every hour
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();

        confirmationTokenRepository.deleteByExpiresAtBefore(now);
        refreshTokenRepository.deleteByExpiryDateBefore(now);
    }
}