package de.upteams.tasktracker.invitation.entity;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an invitation to join a project.
 *
 * <p>Invitations can be sent to both registered and unregistered users.
 * Each invitation has a unique token, expiration time (72 hours), and status.
 * When accepted, a Collaborator is created for the user in the project.</p>
 *
 * @see Project
 * @see ProjectRoles
 * @see InvitationStatus
 */
@Entity
@Table(name = "invitation")
@Getter
@Setter
@NoArgsConstructor
public class Invitation extends BaseEntity {

    /**
     * The project this invitation is for.
     * Cannot be null and is fetched eagerly as it's always needed.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    //Email address of the invited user.
    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ProjectRoles role;

    /**
     * Unique token used to identify and validate the invitation.
     * Generated as UUID for uniqueness and security.
     */
    @Column(name = "invite_token", nullable = false, unique = true)
    private UUID inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    /**
     * Date and time when the invitation expires.
     * Default is 72 hours from creation.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Date and time when the invitation was used (accepted).
     * Null if not yet used.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;  // User, if already registered

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    /**
     * Creates a new invitation with default values.
     * Token is auto-generated, expiration set to 72 hours from now.
     *
     * @param project the project to invite to
     * @param email   email of the invited user
     * @param role    role to assign in the project
     */
    public Invitation(Project project, String email, ProjectRoles role) {
        this.project = project;
        this.email = email.toLowerCase().trim(); // Normalize email
        this.role = role;
        this.inviteToken = UUID.randomUUID();
        this.expiresAt = LocalDateTime.now().plusHours(72);
//        this.status = InvitationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.invitedAt = LocalDateTime.now();
    }

    /**
     * Checks if the invitation is still valid.
     * An invitation is valid if it's PENDING and not expired.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return status == InvitationStatus.PENDING &&
                LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Marks the invitation as used and records the usage time.
     * Called when user accepts the invitation.
     */
    public void markAsUsed() {
        this.status = InvitationStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Refreshes the expiration time by adding 72 hours from now.
     * Used when resending an invitation.
     */
    public void refreshExpiration() {
        this.expiresAt = LocalDateTime.now().plusHours(72);
    }

    @Override
    public String toString() {

        return String.format("Invitation{id=%s, email='%s', project='%s', role='%s', status='%s', expiresAt=%s}",
                getId(), email, project, role, status, expiresAt);
    }

}
