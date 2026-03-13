package de.upteams.tasktracker.user.validator;

import de.upteams.tasktracker.user.dto.request.PasswordChangeDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PasswordChangeDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassForValidPasswords() {
        PasswordChangeDto dto = new PasswordChangeDto(
                "Qwerty@123",
                "Test!1234",
                "Test!1234"
        );

        Set<ConstraintViolation<PasswordChangeDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "NULL, 'Test!1234', 'Test!1234', Current password must not be blank",
            "'Qwerty@123', '', 'Test!1234', New password must not be blank",
            "'Qwerty@123', 'Test!1234', '', New password confirmation must not be blank",
            "'Qwerty@123', 'Test!1234', 'Test!5678', New password and confirmation do not match"
    }, nullValues = "NULL")
    void shouldFailWithExpectedMessage(
            String oldPassword,
            String newPassword,
            String newPasswordConfirm,
            String expectedMessage
    ) {
        PasswordChangeDto dto = new PasswordChangeDto(oldPassword, newPassword, newPasswordConfirm);

        Set<ConstraintViolation<PasswordChangeDto>> violations = validator.validate(dto);

        assertTrue(
                violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .anyMatch(message -> message.equals(expectedMessage))
        );
    }

    @Test
    void shouldBindPasswordsMismatchViolationToNewPasswordConfirmField() {
        PasswordChangeDto dto = new PasswordChangeDto(
                "Qwerty@123",
                "Test!1234",
                "Test!5678"
        );

        Set<ConstraintViolation<PasswordChangeDto>> violations = validator.validate(dto);

        assertTrue(
                violations.stream().anyMatch(v ->
                        v.getMessage().equals("New password and confirmation do not match")
                                && v.getPropertyPath().toString().equals("newPasswordConfirm"))
        );
    }

    @Test
    void shouldFailWhenCurrentPasswordContainsOnlySpaces() {
        PasswordChangeDto dto = new PasswordChangeDto(
                "     ",
                "Test@1234",
                "Test@1234"
        );

        Set<ConstraintViolation<PasswordChangeDto>> violations = validator.validate(dto);

        assertTrue(
                violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .anyMatch(message -> message.equals("Current password must not be blank"))
        );
    }

    @Test
    void shouldReturnAllRelevantViolationsWhenSeveralFieldsAreInvalid() {
        PasswordChangeDto dto = new PasswordChangeDto("", "", "");

        Set<ConstraintViolation<PasswordChangeDto>> violations = validator.validate(dto);
        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertAll(
                () -> assertTrue(messages.contains("Current password must not be blank")),
                () -> assertTrue(messages.contains("New password must not be blank")),
                () -> assertTrue(messages.contains("New password confirmation must not be blank"))
        );
    }
}