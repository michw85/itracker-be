package de.upteams.tasktracker.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Project summary for dashboard")
public class ProjectSummaryDto {
    private UUID id;
    private String title;
    private String description;
    private int activeTasksCount;
    private int executorsCount;
    private String status; // OPEN, CLOSED, etc.
}