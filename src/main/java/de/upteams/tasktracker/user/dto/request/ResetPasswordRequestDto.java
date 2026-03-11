package de.upteams.tasktracker.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDto {
    private String token;
    private String newPassword;
}
