package com.ahmed.authservice.email;

/**
 * Abstraction for sending emails.
 * Allows decoupling of email logic from implementation,
 * making it easier to switch providers (SMTP, API-based services, etc.).
 */
public interface EmailSender {
    /**
     * Sends an email.
     * @param to recipient email address
     * @param email HTML content of the email
     */
    void send(String to , String email);
}
