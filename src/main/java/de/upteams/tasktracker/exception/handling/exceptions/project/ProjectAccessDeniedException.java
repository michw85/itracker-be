package de.upteams.tasktracker.exception.handling.exceptions.project;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProjectAccessDeniedException extends RuntimeException {
    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}