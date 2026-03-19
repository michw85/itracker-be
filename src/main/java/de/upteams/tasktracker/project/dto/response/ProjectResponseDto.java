package de.upteams.tasktracker.project.dto.response;

import de.upteams.tasktracker.user.dto.EmployeeDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Project DTO
 *
 * @param id          Project ID
 * @param title       Project title
 * @param description Project description
 * @param ownerId     ID of the Project owner
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectResponseDto(
        @Schema(
                description = "Unique identifier of the Project",
                example = "123e4567-e89b-12d3-a456-426614174000",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID id,

        @Schema(
                description = "Title of the Project",
                example = "New Website Development"
        )
        String title,

        @Schema(
                description = "Detailed description of the Project",
                example = "A Project to develop a new company website"
        )
        String description,

        @Schema(
                description = "The User who created the Project",
                example = "123e4567-e89b-12d3-a456-426614174001",
                accessMode = Schema.AccessMode.READ_ONLY)
        UUID ownerId) {

}
