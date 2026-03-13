package de.upteams.tasktracker.user.entity;

import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)

public class PasswordResetToken extends BaseEntity {


    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = AppUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder
    public PasswordResetToken(String token, AppUser user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusMinutes(60);
    }
    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "token='" + token + '\'' +
                ", user=" + (user != null ? user.getEmail() : "null") +
                ", expiryDate=" + expiryDate +
                '}';
    }
}