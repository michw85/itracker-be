package de.upteams.tasktracker.user.dto.request;

import de.upteams.tasktracker.user.validator.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
        @Schema(
                description = "User's email address",
                example = "tes_dev@upteams.de",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(
                description = "User's display name",
                example = "John Doe"
        )
        @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
        String displayName,

        @Schema(
                description = "User's password",
                example = "dev_TR_pass_007",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Password is required")
        @ValidPassword
        String password) {
}
