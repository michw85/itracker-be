package de.upteams.tasktracker.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "User role in project response")
public class ProjectRoleResponse {
    @Schema(description = "User role in project", example = "MEMBER")
    private String role;
}