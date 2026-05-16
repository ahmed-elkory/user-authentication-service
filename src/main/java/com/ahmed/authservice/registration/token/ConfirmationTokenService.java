package com.ahmed.authservice.registration.token;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service responsible for managing email confirmation tokens.

 * Handles:
 * - Saving newly generated confirmation tokens
 * - Retrieving tokens during email verification
 * - Marking tokens as confirmed

 * This service is used in the user registration flow to validate
 * and activate user accounts via email confirmation.
 */
@Service
@AllArgsConstructor
public class ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    /**
     * Persists a newly created confirmation token in the database.

     * @param token confirmation token entity to be saved
     */
    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }
    /**
     * Retrieves a confirmation token by its value.

     * Used during email verification to validate user ownership.
     *
     * @param token raw confirmation token string
     * @return Optional containing the token if found
     */
    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    /**
     * Marks a confirmation token as confirmed by setting the confirmation timestamp.

     * This indicates that the user has successfully verified their email address.
     *
     * @param token confirmation token string
     * @throws IllegalStateException if token does not exist
     */
    @Transactional
    public void setConfirmedAt(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("token not found"));

        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
    }
}
