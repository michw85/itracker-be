package de.upteams.tasktracker.project.dto;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project member response")
public class MemberResponseDto {
    @Schema(description = "Member ID (invitation or collaborator ID)")
    private UUID id;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "Role in project")
    private ProjectRoles role;

    @Schema(description = "Member status (ACTIVE/PENDING)")
    private String status;

    @Schema(description = "When invitation was sent")
    private LocalDateTime invitedAt;

    @Schema(description = "When invitation expires")
    private LocalDateTime expiresAt;

    @Schema(description = "User ID (if registered)")
    private UUID userId;
}
