package de.upteams.tasktracker.invitation.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for invitation acceptance.
 * Returned after successfully accepting an invitation.
 */
@Data
@Builder
@Schema(description = "Invitation acceptance response")

public class InvitationAcceptResponseDto {

    @Schema(description = "Success message")
    private String message;

    @Schema(description = "Whether acceptance was successful")
    private boolean success;

    @Schema(description = "Project ID")
    private String projectId;

    @Schema(description = "Project name")
    private String projectName;
}
