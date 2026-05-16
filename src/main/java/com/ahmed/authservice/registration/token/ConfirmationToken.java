package com.ahmed.authservice.registration.token;

import com.ahmed.authservice.appuser.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Represents email verification token.

 * Used to confirm user email during registration.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "confirmation_token",
        indexes = {
                @Index(name = "idx_token", columnList = "token")
        }
)
public class ConfirmationToken {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true)
        private String token;

        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime confirmedAt;

        @ManyToOne
        @JoinColumn(nullable = false, name = "app_user_id")
        private AppUser appUser;

    public ConfirmationToken(String token, LocalDateTime createdAt, LocalDateTime expiresAt,AppUser appUser) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.appUser = appUser;
    }

}
