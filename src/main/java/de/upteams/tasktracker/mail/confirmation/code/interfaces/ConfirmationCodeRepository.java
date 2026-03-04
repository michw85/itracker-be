package de.upteams.tasktracker.mail.confirmation.code.interfaces;

import de.upteams.tasktracker.mail.confirmation.code.ConfirmationCode;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmationCodeRepository extends JpaRepository<ConfirmationCode, UUID> {

    @Query("select c from ConfirmationCode c where c.id = ?1")
    Optional<ConfirmationCode> findByCode(UUID code);

    @Query("select c from ConfirmationCode c where c.user = ?1")
    Optional<ConfirmationCode> findByUser(AppUser user);
}
