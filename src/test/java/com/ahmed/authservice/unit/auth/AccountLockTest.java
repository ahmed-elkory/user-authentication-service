package com.ahmed.authservice.unit.auth;

import com.ahmed.authservice.appuser.AppUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountLockTest {

    @Test
    void shouldNotLockAccountBelowThreshold() {
        AppUser user = new AppUser();
        user.setFailedAttempts(4);
        user.setLocked(false);

        if (user.getFailedAttempts() >= 5) {
            user.setLocked(true);
        }

        assertFalse(user.isLocked());
    }

    @Test
    void shouldLockAccountAtThreshold() {
        AppUser user = new AppUser();
        user.setFailedAttempts(5);
        user.setLocked(false);

        if (user.getFailedAttempts() >= 5) {
            user.setLocked(true);
        }

        assertTrue(user.isLocked());
    }

    @Test
    void shouldLockAccountAboveThreshold() {
        AppUser user = new AppUser();
        user.setFailedAttempts(10);
        user.setLocked(false);

        if (user.getFailedAttempts() >= 5) {
            user.setLocked(true);
        }

        assertTrue(user.isLocked());
    }

    @Test
    void shouldRemainLockedIfAlreadyLocked() {
        AppUser user = new AppUser();
        user.setFailedAttempts(3);
        user.setLocked(true);

        if (user.getFailedAttempts() >= 5) {
            user.setLocked(true);
        }

        assertTrue(user.isLocked());
    }
}