package de.upteams.tasktracker.user.dto.response;

import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO returned when fetching user data.
 */
@Schema(description = "User data returned by the API")
public record UserResponseDto(
        @Schema(
                description = "User's email address",
                example     = "homer@simpsons.com"
        )
        String email,

        @Schema(
                description = "Role assigned to the user",
                example     = "ROLE_USER",
                accessMode  = Schema.AccessMode.READ_ONLY
        )
        String role,

        @Schema(
                description = "Confirmation status of the user account",
                example     = "UNCONFIRMED",
                accessMode  = Schema.AccessMode.READ_ONLY
        )
        ConfirmationStatus confirmationStatus
) {}
