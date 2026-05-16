package com.ahmed.authservice.appuser;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;


/**
 * Represents an application user entity.

 * This class implements Spring Security's UserDetails interface
 * to integrate directly with the authentication framework.

 * It stores authentication-related data such as credentials,
 * account status (locked, enabled), and audit fields.
 */


@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(
        indexes = @Index(name = "idx_email", columnList = "email")
)
@Entity
public class AppUser implements UserDetails {
    /**
     * Primary key generated using database sequence.
     */
    @EqualsAndHashCode.Include
    @Id
    @SequenceGenerator(
            name = "app_user_sequence",
            sequenceName = "app_user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "app_user_sequence"
    )
    private long id;
    private String firstName;
    private String lastName;
    /**
     * Unique email used as username for authentication.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /** Hashed password (BCrypt) */
    private String password;

    /** Role assigned to the user (USER / ADMIN) */
    @Enumerated(EnumType.STRING)
    private AppUserRole appUserRole;

    /** Indicates whether the account is locked due to failed attempts */
    @Column(nullable = false)
    private boolean locked = false;

    /** Indicates whether the user has verified their email */
    @Column(nullable = false)
    private boolean enabled = false;

    /** Number of consecutive failed login attempts */
    private int failedAttempts = 0;

    /** Timestamp when account was locked */
    @Column
    private LocalDateTime lockTime;

    /** Audit: creation timestamp */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** Audit: last update timestamp */
    private LocalDateTime updatedAt;

    /** Audit: last successful login */
    private LocalDateTime lastLogin;


    public AppUser(String firstName, String lastName, String email, String password, AppUserRole appUserRole) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.appUserRole = appUserRole;
    }

    /**
     * Returns user authorities based on assigned role.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + this.appUserRole.name());
        return Collections.singletonList(authority);
    }


    /**
     * Gets email password for authentication.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Uses email as username for authentication.
     */
    @Override
    public String getUsername() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Account is considered non-locked if 'locked' is false.
     */
    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is allowed to authenticate.
     * Typically set after email verification.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Automatically set timestamps when entity is created.
     */
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically update timestamp on entity update.
     */
    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
