package de.upteams.tasktracker.task.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.user.dto.EmployeeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * Task DTO
 */
@Schema(description = "Data Transfer Object for Task entity")
@Value
public class TaskDto {

    @Schema(
            description = "Unique identifier of the Task",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    String id;

    @Schema(description = "Title of the Task", example = "Implement repository layer")
    String title;

    @Schema(
            description = "Detailed description of the Task",
            example = "Create JPA repositories for all entities"
    )
    String description;

    @JsonIgnore
    @Schema(
            description = "The Project whit which this Task is associated",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    ProjectResponseDto project;

    @Schema(
            description = "List of Users assigned to this Task",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    Set<EmployeeDto> executors = new HashSet<>();

}
