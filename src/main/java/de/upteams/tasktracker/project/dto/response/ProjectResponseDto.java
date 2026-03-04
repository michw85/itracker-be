package de.upteams.tasktracker.project.dto.response;

import de.upteams.tasktracker.user.dto.EmployeeDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Project DTO
 *
 * @param id          Project ID
 * @param title       Project title
 * @param description Project description
 * @param owner       Author of the Project
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectResponseDto(
        @Schema(
                description = "Unique identifier of the Project",
                example = "7",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String id,

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
                accessMode = Schema.AccessMode.READ_ONLY)
        EmployeeDto owner) {

}
