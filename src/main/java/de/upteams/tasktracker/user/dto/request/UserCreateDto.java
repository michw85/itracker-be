package de.upteams.tasktracker.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserCreateDto(
        @Schema(
                description = "new User email",
                example = "tes_dev@upteams.de"
        )
        String email,

        @Schema(
                description = "new User password",
                example = "dev_TR_pass_007"
        )
        String password) {
}
