package de.upteams.tasktracker.invitation.repository;

import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import de.upteams.tasktracker.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Invitation entities.
 * Provides methods for finding invitations by various criteria.
 */
@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    // Finds an invitation by its unique token.
    Optional<Invitation> findByInviteToken(UUID token);

    /**
     * Finds a pending invitation for a specific project and email.
     * Used to check if an invitation already exists before creating a new one.
     *
     * @param project the project
     * @param email   the email address
     * @param status  the status (should be PENDING)
     * @return Optional containing the invitation if found
     */
    @Query("SELECT i FROM Invitation i WHERE i.project = :project AND i.email = :email AND i.status = :status")
    Optional<Invitation> findPendingByProjectAndEmail(
            @Param("project") Project project,
            @Param("email") String email,
            @Param("status") InvitationStatus status
    );

    /**
     * Finds all valid (not expired) pending invitations for a given email.
     * Used when a user registers to automatically add them to projects.
     *
     * @param email  the email address
     * @param status the status (should be PENDING)
     * @param now    current time for expiration check
     * @return list of valid invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.status = :status AND i.expiresAt > :now")
    List<Invitation> findAllValidByEmail(
            @Param("email") String email,
            @Param("status") InvitationStatus status,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT i FROM Invitation i WHERE i.project.id = :projectId")
    List<Invitation> findByProjectId(@Param("projectId") UUID projectId);

    // Добавьте для отладки:
    @Query("SELECT COUNT(i) FROM Invitation i WHERE i.project.id = :projectId")
    long countByProjectId(@Param("projectId") UUID projectId);

    // Find all participants (accepted invitations) of a project
    @Query("SELECT i FROM Invitation i WHERE i.project.id = :projectId AND i.status = 'USED'")
    List<Invitation> findAcceptedByProjectId(@Param("projectId") UUID projectId);

    //Find pending project invitations
    @Query("SELECT i FROM Invitation i WHERE i.project.id = :projectId AND i.status = 'PENDING'")
    List<Invitation> findPendingByProjectId(@Param("projectId") UUID projectId);
}
