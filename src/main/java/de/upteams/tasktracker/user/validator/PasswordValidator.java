package de.upteams.tasktracker.user.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

import static de.upteams.tasktracker.user.constants.UserValidationConstants.SPECIAL_SYMBOLS;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isBlank()) {
            errors.add("{user.password.blank}");
        } else {
            if (password.length() < 8) {
                errors.add("{user.password.size}");
            }

            boolean hasUppercase = false;
            boolean hasLowercase = false;
            boolean hasDigit = false;
            boolean hasSpecial = false;

            for (int i = 0; i < password.length(); i++) {
                char current = password.charAt(i);

                if (current >= 'A' && current <= 'Z') {
                    hasUppercase = true;
                } else if (current >= 'a' && current <= 'z') {
                    hasLowercase = true;
                } else if (current >= '0' && current <= '9') {
                    hasDigit = true;
                } else if (SPECIAL_SYMBOLS.indexOf(current) >= 0) {
                    hasSpecial = true;
                } else {
                    errors.add("{user.password.invalid.characters}");
                }
            }

            if (!hasUppercase) {
                errors.add("{user.password.uppercase}");
            }

            if (!hasLowercase) {
                errors.add("{user.password.lowercase}");
            }

            if (!hasDigit) {
                errors.add("{user.password.digit}");
            }

            if (!hasSpecial) {
                errors.add("{user.password.special}");
            }
        }

        if (errors.isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        for (String error : errors) {
            context.buildConstraintViolationWithTemplate(error)
                    .addConstraintViolation();
        }

        return false;
    }
}
