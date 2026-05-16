package com.ahmed.authservice.bootstrap;

import com.ahmed.authservice.appuser.AppUser;
import com.ahmed.authservice.appuser.AppUserRepository;
import com.ahmed.authservice.appuser.AppUserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer {

    private final AppUserRepository repo;
    private final BCryptPasswordEncoder encoder;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    CommandLineRunner init() {
        return args -> {

            if (repo.findByEmail(adminEmail).isEmpty()) {

                AppUser admin = new AppUser();

                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setEmail(adminEmail);
                admin.setPassword(encoder.encode(adminPassword));
                admin.setAppUserRole(AppUserRole.ADMIN);
                admin.setEnabled(true);
                admin.setLocked(false);
                admin.setFailedAttempts(0);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());

                repo.save(admin);

                log.info("ADMIN CREATED");
            }
        };
    }
}
