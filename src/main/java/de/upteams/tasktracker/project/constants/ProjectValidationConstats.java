package de.upteams.tasktracker.project.constants;

public final class ProjectValidationConstats {

    private ProjectValidationConstats() {
        throw new IllegalStateException("Utility class");
    }

    public static final String NAME_REGEX = "[a-zA-Z1-9 ]";
    public static final int NAME_MAX_LENGTH = 155;
    public static final int NAME_MIN_LENGTH = 155;
}
