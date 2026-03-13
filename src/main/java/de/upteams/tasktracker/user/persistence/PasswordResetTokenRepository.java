package de.upteams.tasktracker.user.persistence;

import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(AppUser user);
}
