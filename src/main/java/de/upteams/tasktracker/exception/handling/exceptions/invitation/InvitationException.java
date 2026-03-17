package de.upteams.tasktracker.exception.handling.exceptions.invitation;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invitation operation fails.
 * Extends RestApiException to leverage global exception handling.
 */

public class InvitationException extends RestApiException{
    private static final String DEFAULT_MESSAGE = "Invitation operation failed";

    /**
     * Creates a new InvitationException with default message.
     */
    public InvitationException() {
        super(HttpStatus.BAD_REQUEST, DEFAULT_MESSAGE);
    }

    /**
     * Creates a new InvitationException with custom message.
     *
     * @param message detailed error message
     */
    public InvitationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Creates a new InvitationException with custom HTTP status and message.
     *
     * @param status  HTTP status code
     * @param message detailed error message
     */
    public InvitationException(HttpStatus status, String message) {
        super(status, message);
    }

    /**
     * Factory method for invalid/expired token errors.
     *
     * @return InvitationException with appropriate message
     */
    public static InvitationException invalidToken() {
        return new InvitationException(
                HttpStatus.BAD_REQUEST,
                "Invitation token is invalid or expired"
        );
    }

    /**
     * Factory method for already member errors.
     *
     * @return InvitationException with appropriate message
     */
    public static InvitationException alreadyMember() {
        return new InvitationException(
                HttpStatus.CONFLICT,
                "User is already a member of this project"
        );
    }

    /**
     * Factory method for permission errors.
     *
     * @return InvitationException with appropriate message
     */
    public static InvitationException noPermission() {
        return new InvitationException(
                HttpStatus.FORBIDDEN,
                "You don't have permission to invite users to this project"
        );
    }
}
