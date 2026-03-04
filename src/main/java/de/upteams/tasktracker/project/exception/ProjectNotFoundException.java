package de.upteams.tasktracker.project.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception that throws when Project does not exist in the Database
 */
public class ProjectNotFoundException extends RestApiException {

    public ProjectNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Project not found");
    }
}
