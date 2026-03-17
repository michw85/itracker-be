package de.upteams.tasktracker.invitation.dto.request;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for creating or updating a project invitation.
 * Contains the email of the user to invite and the role to assign.
 */
@Data
@Schema(description = "Request to invite a user to a project")
public class InvitationRequestDto {
    /**
     * Email address of the user to invite.
     * Must be a valid email format and not null.
     */
    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email of the user to invite", example = "user@example.com")
    private String email;

    /**
     * Role to assign to the user in the project.
     * Cannot be null.
     */
    @NotNull(message = "Role is required")
    @Schema(description = "Role to assign in the project", example = "MEMBER")
    private ProjectRoles role;
}
