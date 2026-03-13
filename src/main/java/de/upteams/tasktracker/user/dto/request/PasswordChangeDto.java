package de.upteams.tasktracker.user.dto.request;

import de.upteams.tasktracker.user.validator.annotation.PasswordsMatch;
import de.upteams.tasktracker.user.validator.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for changing user password")
@PasswordsMatch
public record PasswordChangeDto(

        @Schema(description = "User's old password", example = "Password@123")
        @NotBlank(message = "Current password must not be blank")
        String oldPassword,

        @Schema(description = "User's new password", example = "Password@456")
        @NotBlank(message = "New password must not be blank")
        @ValidPassword
        String newPassword,

        @Schema(description = "User's new password confirmation. Should match with new password", example = "Password@456")
        @NotBlank(message = "New password confirmation must not be blank")
        String newPasswordConfirm
) {
}
