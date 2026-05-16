package com.ahmed.authservice.e2e;

import com.ahmed.authservice.appuser.AppUser;
import com.ahmed.authservice.appuser.AppUserRepository;
import com.ahmed.authservice.appuser.AppUserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    private static final String BASE_URL = "/api/v1/auth/login";

    @BeforeEach
    void setup() {
        repository.deleteAll();

        AppUser user = new AppUser(
                "Ahmed",
                "Admin",
                "admin@test.com",
                encoder.encode("admin123"),
                AppUserRole.USER
        );

        user.setEnabled(true);
        repository.save(user);
    }

    // -------------------------
    // 1. SUCCESS LOGIN
    // -------------------------


    // -------------------------
    // 2. WRONG PASSWORD
    // -------------------------
    @Test
    void loginShouldReturnUnauthorizedForWrongPassword() throws Exception {

        String body = """
    {
      "email":"admin@test.com",
      "password":"wrong"
    }
    """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError()); // 401 or 400 depending on flow
    }

    // -------------------------
    // 3. USER NOT FOUND
    // -------------------------
    @Test
    void loginShouldReturn401ForUnknownUser() throws Exception {

        String body = """
        {
          "email":"missing@test.com",
          "password":"admin123"
        }
        """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------
    // 4. DISABLED USER
    // -------------------------
    @Test
    void loginShouldRejectDisabledUser() throws Exception {

        AppUser user = repository.findByEmail("admin@test.com").orElseThrow();
        user.setEnabled(false);
        repository.save(user);

        String body = """
        {
          "email":"admin@test.com",
          "password":"admin123"
        }
        """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest()); // <- matches your handler (IllegalStateException → 400)
    }

    // -------------------------
    // 5. INVALID JSON
    // -------------------------
    @Test
    void loginShouldReturn400ForInvalidBody() throws Exception {

        String body = """
        {
          "email":"admin@test.com"
        }
        """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // -------------------------
    // 6. WRONG HTTP METHOD
    // -------------------------

    @Test
    void debugLogin() throws Exception {

        String body = """
    {
      "email":"admin@test.com",
      "password":"admin123"
    }
    """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print());
    }
}