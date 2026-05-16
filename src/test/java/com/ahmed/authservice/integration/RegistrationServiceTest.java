package com.ahmed.authservice.integration;

import com.ahmed.authservice.appuser.AppUserRepository;
import com.ahmed.authservice.email.EmailService;
import com.ahmed.authservice.registration.RegistrationRequest;
import com.ahmed.authservice.registration.RegistrationService;
import com.ahmed.authservice.registration.token.ConfirmationTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private ConfirmationTokenRepository tokenRepository;

    @MockBean
    private EmailService emailService;
    @MockBean
    private org.springframework.mail.javamail.JavaMailSender mailSender;


    @Test
    void shouldRegisterUserSuccessfully() {

        RegistrationRequest request = new RegistrationRequest(
                "Ahmed",
                "Test",
                "new@test.com",
                "Password1!"
        );

        registrationService.register(request);

        assertTrue(userRepository.findByEmail("new@test.com").isPresent());
    }

    @Test
    void shouldGenerateConfirmationToken() {

        RegistrationRequest request = new RegistrationRequest(
                "Ahmed",
                "Test",
                "token@test.com",
                "Password1!"
        );

        registrationService.register(request);

        assertFalse(tokenRepository.findAll().isEmpty());
    }

    @Test
    void shouldRejectDuplicateEmail() {

        RegistrationRequest request = new RegistrationRequest(
                "Ahmed",
                "Test",
                "duplicate@test.com",
                "Password1!"
        );

        registrationService.register(request);

        assertThrows(Exception.class, () ->
                registrationService.register(request)
        );
    }

    @Test
    void shouldCreateDisabledUserByDefault() {

        RegistrationRequest request = new RegistrationRequest(
                "Ahmed",
                "Test",
                "disabled@test.com",
                "Password1!"
        );

        registrationService.register(request);

        var user = userRepository.findByEmail("disabled@test.com").orElseThrow();

        assertFalse(user.isEnabled());
    }

    @Test
    void shouldEncodePasswordOnRegistration() {

        RegistrationRequest request = new RegistrationRequest(
                "Ahmed",
                "Test",
                "encode@test.com",
                "Password1!"
        );

        registrationService.register(request);

        var user = userRepository.findByEmail("encode@test.com").orElseThrow();

        assertNotEquals("Password1!", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$")); // bcrypt check
    }


    @Test
    void shouldGenerateValidToken() {

        RegistrationRequest request = new RegistrationRequest(
                "Ahmed",
                "Test",
                "validtoken@test.com",
                "Password1!"
        );

        registrationService.register(request);

        var token = tokenRepository.findAll().stream().findFirst().orElseThrow();

        assertNotNull(token.getToken());
        assertFalse(token.getToken().isBlank());
    }




}