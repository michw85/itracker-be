package de.upteams.tasktracker.user.validator;

import de.upteams.tasktracker.user.validator.annotation.ValidPassword;
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

class PasswordValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    static class TestPasswordDto {

        @ValidPassword
        private String password;

        TestPasswordDto(String password) {
            this.password = password;
        }
    }

    @Test
    void shouldPassForValidPassword() {
        TestPasswordDto dto = new TestPasswordDto("Qwerty1@");

        Set<ConstraintViolation<TestPasswordDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            ", Password must not be blank",
            "Qwe1!, Password must be at least 8 characters long",
            "Qwe tyui1!, 'Password contains invalid characters. Only Latin letters, digits, and special symbols are allowed'",
            "qwerty1!, Password must contain at least one uppercase Latin letter",
            "QWERTY1!, Password must contain at least one lowercase Latin letter",
            "Qwertyu!, Password must contain at least one digit",
            "Qwertyu1, Password must contain at least one special symbol"
    })
    void shouldFailWithExpectedMessage(String password, String expectedMessage) {
        TestPasswordDto dto = new TestPasswordDto(password);

        Set<ConstraintViolation<TestPasswordDto>> violations = validator.validate(dto);

        assertTrue(
                violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .anyMatch(message -> message.equals(expectedMessage))
        );
    }

    @Test
    void shouldReturnAllRelevantMessagesWhenPasswordHasMultipleViolations() {
        TestPasswordDto dto = new TestPasswordDto("1234Я5678");

        Set<ConstraintViolation<TestPasswordDto>> violations = validator.validate(dto);
        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertFalse(messages.isEmpty());

        assertTrue(messages.contains("Password must contain at least one uppercase Latin letter"));
        assertTrue(messages.contains("Password must contain at least one lowercase Latin letter"));
        assertTrue(messages.contains("Password must contain at least one special symbol"));
        assertTrue(messages.contains("Password contains invalid characters. Only Latin letters, digits, and special symbols are allowed"));

        assertFalse(messages.contains("Password must be at least 8 characters long"));
        assertFalse(messages.contains("Password must contain at least one digit"));
        assertFalse(messages.contains("Password must not be blank"));
    }
}