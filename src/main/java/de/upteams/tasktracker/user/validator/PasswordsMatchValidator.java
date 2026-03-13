package de.upteams.tasktracker.user.validator;

import de.upteams.tasktracker.user.dto.request.PasswordChangeDto;
import de.upteams.tasktracker.user.validator.annotation.PasswordsMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, PasswordChangeDto> {
    @Override
    public boolean isValid(PasswordChangeDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        if (dto.newPassword() == null || dto.newPasswordConfirm() == null) {
            return true;
        }

        boolean matches = dto.newPassword().equals(dto.newPasswordConfirm());

        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{user.password.change.confirm}")
                    .addPropertyNode("newPasswordConfirm")
                    .addConstraintViolation();
        }

        return matches;
    }
}
