package de.upteams.tasktracker.user.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends RestApiException {

    public InvalidPasswordException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
