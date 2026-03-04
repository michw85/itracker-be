package de.upteams.tasktracker.task.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception that throws when Task does not exist in the Database
 */
public class TaskNotFoundException extends RestApiException {

    public TaskNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Task not found");
    }
}
