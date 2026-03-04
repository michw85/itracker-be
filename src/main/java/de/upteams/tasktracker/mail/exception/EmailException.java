package de.upteams.tasktracker.mail.exception;

/**
 * Базовое исключение для всех ошибок, связанных с email.
 */
public class EmailException extends RuntimeException {
    public EmailException(String message) {
        super(message);
    }

    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

