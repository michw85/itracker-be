package de.upteams.tasktracker.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Employee DTO
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
        @Schema(
                description = "Unique identifier of the Employee",
                example = "9",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String id,

        @Schema(
                description = "Employee's name",
                example = "Homer Simpson"
        )
        String name,

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

        @Schema(
                description = "URL of Employee's avatar image",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String avatar,

        @Schema(
                description = "List of Roles granted to this Employee",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        RoleDto roles) {
}
