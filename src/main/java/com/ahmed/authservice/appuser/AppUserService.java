package com.ahmed.authservice.appuser;

import com.ahmed.authservice.registration.token.ConfirmationToken;
import com.ahmed.authservice.registration.token.ConfirmationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service responsible for user management and authentication integration.

 * Implements UserDetailsService to allow Spring Security
 * to load user details during authentication.
 */
@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MESSAGE = "User with email %s not found";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    @Value("${app.token.expiration}")
    private int tokenExpirationMinutes;

    /**
     * Loads user by email for authentication.

     * @param email user's email
     * @return UserDetails object
     * @throws UsernameNotFoundException if user does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MESSAGE,email)));
    }

    /**
     * Registers a new user in the system.

     * Steps:
     * 1. Check if email already exists
     * 2. Encode password
     * 3. Save user
     * 4. Generate email confirmation token

     * @param appUser user to register
     * @return confirmation token
     */
    public String signUpUser(AppUser appUser) {

        boolean userExists = appUserRepository
                .findByEmail(appUser.getEmail())
                .isPresent();

        if (userExists) {
            throw new IllegalStateException("Email already taken");
        }

        // Encrypt password before saving
        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);

        appUserRepository.save(appUser);

        // Generate confirmation token
        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(tokenExpirationMinutes),
                appUser
        );
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }

    /**
     * Enables user account after successful email verification.
     *
     * @param email user's email
     * @return number of updated rows
     */
    public int enableAppUser(String email) {
        return appUserRepository.enableAppUserByEmail(email);
    }

}
