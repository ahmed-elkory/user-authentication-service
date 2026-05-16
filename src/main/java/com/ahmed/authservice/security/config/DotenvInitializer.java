package com.ahmed.authservice.security.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;

/**
 * Loads environment variables from .env file (development only).

 * NOTE: Should not be used in production environments.
 */
public class DotenvInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        try {

            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            setIfPresent("DB_USERNAME", dotenv.get("DB_USERNAME"));
            setIfPresent("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

            setIfPresent("JWT_SECRET", dotenv.get("JWT_SECRET"));
            setIfPresent("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));

            setIfPresent("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
            setIfPresent("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));

            setIfPresent("ADMIN_EMAIL", dotenv.get("ADMIN_EMAIL"));
            setIfPresent("ADMIN_PASSWORD", dotenv.get("ADMIN_PASSWORD"));

        } catch (Exception ignored) {
        }
    }

    private void setIfPresent(String key, String value) {

        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
    }
}