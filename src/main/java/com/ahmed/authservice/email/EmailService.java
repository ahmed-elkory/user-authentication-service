package com.ahmed.authservice.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailSender using Spring's JavaMailSender.

 * Responsible for sending HTML emails via SMTP.
 * Uses asynchronous execution to avoid blocking the main request thread.
 */
@Service
@RequiredArgsConstructor
public class EmailService implements EmailSender {

    private final JavaMailSender mailSender;
    @Value("${app.mail.subject}")
    private String subject;

    @Value("${spring.mail.username}")
    private String fromEmail;


    /**
     * Sends an email asynchronously.
     *
     * This method:
     * - Builds a MIME message (supports HTML content)
     * - Sets recipient, subject, and sender
     * - Sends email via configured SMTP server
     *
     * @param to recipient email address
     * @param email HTML email body
     */
    @Async
    @Override
    public void send(String to, String email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Helper simplifies working with MIME messages
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            // Set email content (true = HTML)
            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);

            mailSender.send(message);

        } catch (MessagingException e) {
            // Wrap low-level exception into application-level exception
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}