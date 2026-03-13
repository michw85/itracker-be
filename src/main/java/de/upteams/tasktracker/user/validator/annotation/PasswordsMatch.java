package de.upteams.tasktracker.user.validator.annotation;

import de.upteams.tasktracker.user.validator.PasswordsMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsMatchValidator.class)
public @interface PasswordsMatch {

    String message() default "{user.password.change.confirm}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
