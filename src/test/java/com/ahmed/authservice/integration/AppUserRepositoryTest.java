package com.ahmed.authservice.integration;

import com.ahmed.authservice.appuser.AppUser;
import com.ahmed.authservice.appuser.AppUserRepository;
import com.ahmed.authservice.appuser.AppUserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository repository;

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {

        AppUser user = new AppUser(
                "Ahmed",
                "Test",
                "test@test.com",
                "password",
                AppUserRole.USER
        );

        repository.save(user);

        var result = repository.findByEmail("test@test.com");

        assertTrue(result.isPresent());
        assertEquals("Ahmed", result.get().getFirstName());
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailNotFound() {

        var result = repository.findByEmail("missing@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {

        AppUser user = new AppUser(
                "Ahmed",
                "Test",
                "save@test.com",
                "password",
                AppUserRole.USER
        );

        AppUser savedUser = repository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("save@test.com", savedUser.getEmail());
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void shouldEnforceUniqueEmailConstraint() {

        AppUser firstUser = new AppUser(
                "Ahmed",
                "One",
                "duplicate@test.com",
                "password",
                AppUserRole.USER
        );

        AppUser secondUser = new AppUser(
                "Ahmed",
                "Two",
                "duplicate@test.com",
                "password",
                AppUserRole.USER
        );

        repository.save(firstUser);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    repository.saveAndFlush(secondUser);
                }
        );
    }

    @Test
    @DisplayName("Should update existing user")
    void shouldUpdateExistingUser() {

        AppUser user = new AppUser(
                "Ahmed",
                "Test",
                "update@test.com",
                "password",
                AppUserRole.USER
        );

        AppUser savedUser = repository.save(user);

        savedUser.setFirstName("Updated");

        repository.save(savedUser);

        var updatedUser = repository.findByEmail("update@test.com");

        assertTrue(updatedUser.isPresent());
        assertEquals("Updated", updatedUser.get().getFirstName());
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {

        AppUser user = new AppUser(
                "Ahmed",
                "Test",
                "delete@test.com",
                "password",
                AppUserRole.USER
        );

        AppUser savedUser = repository.save(user);

        repository.delete(savedUser);

        var result = repository.findByEmail("delete@test.com");

        assertTrue(result.isEmpty());
    }
}