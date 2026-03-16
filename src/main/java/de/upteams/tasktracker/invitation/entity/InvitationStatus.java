package de.upteams.tasktracker.invitation.entity;

/**
 * Represents the possible states of an invitation throughout its lifecycle.
 * PENDING - Invitation created and waiting for user action
 * USED - Invitation has been accepted and can no longer be used
 * EXPIRED - Invitation passed its expiration date without being used
 */
public enum InvitationStatus {
    PENDING,
    USED,
    EXPIRED
}
