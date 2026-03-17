package de.upteams.tasktracker.invitation.mapper;
import de.upteams.tasktracker.invitation.dto.response.InvitationResponseDto;
import de.upteams.tasktracker.invitation.entity.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between Invitation entities and DTOs.
 * Uses component model "spring" for dependency injection.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvitationMapper {
    /**
     * Converts Invitation entity to InvitationResponseDto.
     * Maps project.id and project.title to separate fields for client convenience.
     *
     * @param invitation the invitation entity
     * @return the response DTO
     */
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.title")
    InvitationResponseDto toResponseDto(Invitation invitation);
}
