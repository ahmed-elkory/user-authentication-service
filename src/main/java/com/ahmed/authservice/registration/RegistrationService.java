package com.ahmed.authservice.registration;

import com.ahmed.authservice.appuser.AppUser;
import com.ahmed.authservice.appuser.AppUserRole;
import com.ahmed.authservice.appuser.AppUserService;
import com.ahmed.authservice.email.EmailSender;
import com.ahmed.authservice.registration.token.ConfirmationToken;
import com.ahmed.authservice.registration.token.ConfirmationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handles user registration and email confirmation workflow.

 * Responsibilities:
 * - Create new user accounts
 * - Generate email verification tokens
 * - Send confirmation emails
 * - Activate accounts upon verification
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final EmailTemplateService emailTemplateService;


    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Registers a new user and sends email confirmation link.

     * Flow:
     * 1. Create user
     * 2. Generate token
     * 3. Build confirmation link
     * 4. Send email
     */
    public void register(RegistrationRequest request) {
        String token = appUserService.signUpUser(
                new AppUser(
                        request.firstName(),
                        request.lastName(),
                        request.email(),
                        request.password(),
                        AppUserRole.USER
                )
        );

        String link = buildLink(token);

        String emailContent = emailTemplateService.buildEmail(
                request.firstName(),
                link
        );

        emailSender.send(
                request.email(),
                emailContent
        );
        log.info("User registered successfully: {}", request.email());
    }

    private String buildLink(String token) {
        return String.format("%s/api/v1/registration/confirm?token=%s", baseUrl, token);
    }


    /**
     * Confirms email using token.
     *
     * Handles:
     * - Already confirmed tokens
     * - Expired tokens (resends new email)
     * - Successful activation
     */
    @Transactional
    public String confirmToken(String token) {

        var confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Email already confirmed");
        }

        LocalDateTime now = LocalDateTime.now();

        // Handle expiration
        if (confirmationToken.getExpiresAt().isBefore(now)) {

            String email = confirmationToken.getAppUser().getEmail();

            String newToken = UUID.randomUUID().toString();

            ConfirmationToken newConfirmationToken = new ConfirmationToken(
                    newToken,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(15),
                    confirmationToken.getAppUser()
            );

            confirmationTokenService.saveConfirmationToken(newConfirmationToken);

            String link = String.format("%s/api/v1/registration/confirm?token=%s", baseUrl, newToken);

            emailSender.send(
                    email,
                    emailTemplateService.buildEmail(
                            confirmationToken.getAppUser().getFirstName(),
                            link
                    )
            );

            throw new IllegalStateException("Token expired. A new confirmation email has been sent.");
        }

        // Confirm token and enable user
        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());

        log.info("Email confirmed and user enabled for token: {}", token);

        return "confirmed";
    }
}


