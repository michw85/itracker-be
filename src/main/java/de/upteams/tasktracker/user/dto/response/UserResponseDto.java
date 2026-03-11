package de.upteams.tasktracker.user.dto.response;

import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO returned when fetching user data.
 */
@Schema(description = "User data returned by the API")
public record UserResponseDto(
        // TODO: Decide final ID format (Long vs UUID) and update example accordingly
        @Schema(
                description = "User's unique identifier",
                example = "9",          // or "123e4567-e89b-12d3-a456-426614174000",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String id,

        @Schema(
                description = "User's display name",
                example = "Homer Simpsons"
        )
        String displayName,

        @Schema(
                description = "User's email address",
                example     = "homer@simpsons.com"
        )
        String email,

        @Schema(
                description = "User's position in company",
                example = "Frontend Developer"
        )
        String position,

        @Schema(
                description = "User's department",
                example = "Frontend"
        )
        String department,

        @Schema(
                description = "URL to user's avatar image",
                example = "https://storage.example.com/avatars/user123.jpg"
        )
        String avatarUrl,

        @Schema(
                description = "Short biography of the user",
                example = "Frontend developer with 5 years of experience"
        )
        String bio,

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
