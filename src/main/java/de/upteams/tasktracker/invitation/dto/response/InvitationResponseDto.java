package de.upteams.tasktracker.invitation.dto.response;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Invitation details response")
public class InvitationResponseDto {
    @Schema(description = "Invitation ID")
    private UUID id;

    @Schema(description = "Invited user's email")
    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Role in the project")
    @NotNull(message = "Role is required")
    private ProjectRoles role;

    @Schema(description = "Current status")
    private InvitationStatus status;

    @Schema(description = "Unique invitation token")
    private UUID inviteToken;

    @Schema(description = "Expiration date and time")
    private LocalDateTime expiresAt;

    @Schema(description = "Project ID")
    private UUID projectId;

    @Schema(description = "Project name")
    private String projectName;
}
