package de.upteams.tasktracker.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Employee DTO for project assignments
 *
 * @param id       Employee ID
 * @param name     Employee's name
 * @param password Employee's password
 * @param email    Employee's email
 * @param avatar   URL of Employee's avatar image
 * @param roles    Roles of the Employee for authorization process
 */
@Schema(description = "Data Transfer Object for Employee entity")
public record EmployeeDto(
        // TODO: Decide final ID format (Long vs UUID) and update example accordingly
        @Schema(
                description = "Unique identifier of the Employee",
                example = "9",  // or "123e4567-e89b-12d3-a456-426614174000",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String id,

        @Schema(
                description = "Employee's name",
                example = "Homer Simpson"
        )
        String displayName,

        @Schema(
                description = "Employee's password (will be hidden in responses)",
                example = "HomerTheBest123"
        )
        String password,

        @Schema(
                description = "Employee's email",
                example = "homer@simpsons.com"
        )
        String email,

        @Schema(description = "Employee's position", example = "Developer")
        String position,

        @Schema(
                description = "URL of Employee's avatar image",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String avatarUrl,

        @Schema(
                description = "List of Roles granted to this Employee",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        RoleDto roles) {

}
