package de.upteams.tasktracker.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for updating user profile")
public record ProfileUpdateDto(
        @Schema(description = "User's display name", example = "John Doe")
        @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
        String displayName,

        @Schema(description = "User's position", example = "Senior Developer")
        @Size(max = 100, message = "Position must not exceed 100 characters")
        String position,

        @Schema(description = "User's department", example = "Engineering")
        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,

        @Schema(description = "Short biography", example = "Full-stack developer with 5 years of experience")
        @Size(max = 1000, message = "Bio must not exceed 1000 characters")
        String bio
) {}