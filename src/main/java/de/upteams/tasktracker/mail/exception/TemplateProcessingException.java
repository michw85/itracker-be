package de.upteams.tasktracker.mail.exception;

/**
 * Возникает при ошибке генерации HTML из шаблона (например, файл не найден или синтаксическая ошибка).
 */
public class TemplateProcessingException extends EmailException {
    public TemplateProcessingException(String templateName, Throwable cause) {
        super("Failed to process template: " + templateName, cause);
    }
}

