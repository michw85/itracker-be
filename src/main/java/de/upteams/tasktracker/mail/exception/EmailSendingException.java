package de.upteams.tasktracker.mail.exception;

/**
 * Возникает при проблемах с отправкой письма (например, некорректный адрес или ошибка SMTP).
 */
public class EmailSendingException extends EmailException {
    public EmailSendingException(String recipientEmail, Throwable cause) {
        super("Failed to send email to " + recipientEmail, cause);
    }
}

