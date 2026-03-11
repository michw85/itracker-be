package de.upteams.tasktracker.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * DTO representing the response after user registration.
 *
 * <p>This record includes the user's email, role, and a flag indicating whether the
 * confirmation email was resent. It is used to return the response after a successful registration
 * or when a confirmation email has been resent.</p>
 */
public record UserCreateResponseDto(
        // TODO: Decide final ID format (Long vs UUID) and update example accordingly
        @Schema(
                description = "User's unique identifier",
                example = "9",    // or "123e4567-e89b-12d3-a456-426614174000",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String id,

        @Schema(
                description = "User's display name",
                example = "Homer Simpsons"
        )
        String displayName,

        @Schema(
                description = "User email",
                example = "homer@simpsons.com"
        )
        String email,

        @Schema(
                description = "Role granted to this User",
                example = "ROLE_USER",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String role,

        @Schema(
                description = "Flag indicating if the confirmation email was resent. True if the email was resent due to unconfirmed status; false if sent initially.",
                example = "true",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        boolean confirmationResent) {
}
