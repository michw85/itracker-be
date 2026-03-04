package de.upteams.tasktracker.mail.exception;

/**
 * Возникает, если указанный шаблон отсутствует.
 */
public class TemplateNotFoundException extends EmailException {
    public TemplateNotFoundException(String templateName) {
        super("Template not found: " + templateName);
    }
}

