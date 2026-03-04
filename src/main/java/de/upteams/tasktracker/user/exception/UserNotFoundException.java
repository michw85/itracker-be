package de.upteams.tasktracker.user.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RestApiException {

    private static final String MESSAGE = "User not found";

    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, MESSAGE);
    }
}
