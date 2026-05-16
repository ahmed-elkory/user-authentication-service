package com.ahmed.authservice.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service responsible for generating HTML email templates.

 * Uses Thymeleaf template engine to build dynamic email content
 * for user registration and account verification.

 * This ensures separation between email structure (HTML) and business logic.
 */
@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    /**
     * Builds a personalized email content for user registration confirmation.

     * Injects dynamic variables such as:
     * - username
     * - verification link
     *
     * @param name recipient's first name
     * @param link account verification URL
     * @return rendered HTML email content
     */
    public String buildEmail(String name, String link) {

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);

        return templateEngine.process("email-template", context);
    }
}
