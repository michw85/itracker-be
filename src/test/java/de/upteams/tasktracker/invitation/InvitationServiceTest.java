package de.upteams.tasktracker.invitation;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.invitation.InvitationException;
import de.upteams.tasktracker.invitation.dto.request.InvitationRequestDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationResponseDto;
import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import de.upteams.tasktracker.invitation.mapper.InvitationMapper;
import de.upteams.tasktracker.invitation.repository.InvitationRepository;
import de.upteams.tasktracker.invitation.service.InvitationService;
import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.test.util.TestEntityUtils;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvitationService.
 * Uses MockitoExtension to initialize mocks.
 */
@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    // All dependencies must be mocked with @Mock
    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private EmailService emailService;

    @Mock  // Добавляем мок для маппера
    private InvitationMapper invitationMapper;

    // The service under test with all mocked dependencies injected
    @InjectMocks
    private InvitationService invitationService;

    private AppUser owner;
    private AppUser regularUser;
    private Project project;
    private UUID projectId;
    private InvitationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // Create owner with ID using TestEntityUtils
        owner = new AppUser("password", "owner@test.com", "Owner");
        TestEntityUtils.setId(owner, UUID.randomUUID());

        // Create regular user with ID
        regularUser = new AppUser("password", "user@test.com", "User");
        TestEntityUtils.setId(regularUser, UUID.randomUUID());

        // Create project with ID
        project = new Project("Test Project", "Description", owner);
        TestEntityUtils.setId(project, UUID.randomUUID());
        projectId = project.getId();

        // Setup request DTO
        requestDto = new InvitationRequestDto();
        requestDto.setEmail("invited@test.com");
        requestDto.setRole(ProjectRoles.MEMBER);
    }

    @Test
    void createInvitation_ForUnregisteredUser_ShouldSucceed() {
        // Arrange - setup mocks
        when(projectService.getOrTrow(projectId.toString())).thenReturn(project);
        when(userService.getByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(invitationRepository.findPendingByProjectAndEmail(any(Project.class), anyString(), any(InvitationStatus.class)))
                .thenReturn(Optional.empty());

        // Mock the save operation
        Invitation savedInvitation = new Invitation(project, requestDto.getEmail(), requestDto.getRole());
        TestEntityUtils.setId(savedInvitation, UUID.randomUUID());
        when(invitationRepository.save(any(Invitation.class))).thenReturn(savedInvitation);

        // Mock the mapper
        InvitationResponseDto expectedResponse = InvitationResponseDto.builder()
                .id(savedInvitation.getId())
                .email(savedInvitation.getEmail())
                .role(savedInvitation.getRole())
                .status(savedInvitation.getStatus())
                .inviteToken(savedInvitation.getInviteToken())
                .expiresAt(savedInvitation.getExpiresAt())
                .projectId(project.getId())
                .projectName(project.getTitle())
                .build();
        when(invitationMapper.toResponseDto(any(Invitation.class))).thenReturn(expectedResponse);

        // Act
        InvitationResponseDto response = invitationService.createOrUpdateInvitation(
                projectId.toString(), requestDto, owner
        );

        // Assert
        assertNotNull(response);
        assertEquals(requestDto.getEmail(), response.getEmail());
        assertEquals(ProjectRoles.MEMBER, response.getRole());
        assertEquals(InvitationStatus.PENDING, response.getStatus());
        assertNotNull(response.getInviteToken());

        verify(emailService).sendRegistrationInvitationEmail(
                eq(requestDto.getEmail()),
                anyString(),
                anyString()
        );
        verify(invitationRepository).save(any(Invitation.class));
        verify(invitationMapper).toResponseDto(any(Invitation.class));
    }

    @Test
    void createInvitation_ForRegisteredUser_ShouldSucceed() {
        // Arrange
        AppUser invitedUser = new AppUser("password", requestDto.getEmail(), "Invited");
        TestEntityUtils.setId(invitedUser, UUID.randomUUID());

        when(projectService.getOrTrow(projectId.toString())).thenReturn(project);
        when(userService.getByEmail(requestDto.getEmail())).thenReturn(Optional.of(invitedUser));
        when(collaboratorService.isUserInProject(invitedUser, project)).thenReturn(false);
        when(invitationRepository.findPendingByProjectAndEmail(any(Project.class), anyString(), any(InvitationStatus.class)))
                .thenReturn(Optional.empty());

        Invitation savedInvitation = new Invitation(project, requestDto.getEmail(), requestDto.getRole());
        TestEntityUtils.setId(savedInvitation, UUID.randomUUID());
        when(invitationRepository.save(any(Invitation.class))).thenReturn(savedInvitation);

        InvitationResponseDto expectedResponse = InvitationResponseDto.builder()
                .id(savedInvitation.getId())
                .email(savedInvitation.getEmail())
                .role(savedInvitation.getRole())
                .status(savedInvitation.getStatus())
                .inviteToken(savedInvitation.getInviteToken())
                .expiresAt(savedInvitation.getExpiresAt())
                .projectId(project.getId())
                .projectName(project.getTitle())
                .build();
        when(invitationMapper.toResponseDto(any(Invitation.class))).thenReturn(expectedResponse);

        // Act
        InvitationResponseDto response = invitationService.createOrUpdateInvitation(
                projectId.toString(), requestDto, owner
        );

        // Assert
        assertNotNull(response);
        verify(emailService).sendProjectInvitationEmail(
                eq(requestDto.getEmail()),
                anyString(),
                anyString()
        );
        verify(invitationMapper).toResponseDto(any(Invitation.class));
    }

    @Test
    void createInvitation_WhenUserAlreadyMember_ShouldThrowException() {
        // Arrange
        AppUser invitedUser = new AppUser("password", requestDto.getEmail(), "Invited");
        TestEntityUtils.setId(invitedUser, UUID.randomUUID());

        when(projectService.getOrTrow(projectId.toString())).thenReturn(project);
        when(userService.getByEmail(requestDto.getEmail())).thenReturn(Optional.of(invitedUser));
        when(collaboratorService.isUserInProject(invitedUser, project)).thenReturn(true);

        // Act & Assert
        assertThrows(InvitationException.class, () ->
                invitationService.createOrUpdateInvitation(projectId.toString(), requestDto, owner)
        );

        verify(invitationRepository, never()).save(any());
        verify(emailService, never()).sendProjectInvitationEmail(any(), any(), any());
        verify(emailService, never()).sendRegistrationInvitationEmail(any(), any(), any());
        verify(invitationMapper, never()).toResponseDto(any());
    }

    @Test
    void createInvitation_WhenInvitationExists_ShouldUpdateExpiration() {
        // Arrange
        Invitation existingInvitation = new Invitation(project, requestDto.getEmail(), requestDto.getRole());
        TestEntityUtils.setId(existingInvitation, UUID.randomUUID());
        existingInvitation.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired

        when(projectService.getOrTrow(projectId.toString())).thenReturn(project);
        when(userService.getByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(invitationRepository.findPendingByProjectAndEmail(any(Project.class), anyString(), any(InvitationStatus.class)))
                .thenReturn(Optional.of(existingInvitation));
        when(invitationRepository.save(any(Invitation.class))).thenReturn(existingInvitation);

        InvitationResponseDto expectedResponse = InvitationResponseDto.builder()
                .id(existingInvitation.getId())
                .email(existingInvitation.getEmail())
                .role(existingInvitation.getRole())
                .status(existingInvitation.getStatus())
                .inviteToken(existingInvitation.getInviteToken())
                .expiresAt(existingInvitation.getExpiresAt())
                .projectId(project.getId())
                .projectName(project.getTitle())
                .build();
        when(invitationMapper.toResponseDto(any(Invitation.class))).thenReturn(expectedResponse);

        // Act
        InvitationResponseDto response = invitationService.createOrUpdateInvitation(
                projectId.toString(), requestDto, owner
        );

        // Assert
        assertNotNull(response);
        assertFalse(response.getExpiresAt().isAfter(LocalDateTime.now()));
        verify(invitationRepository).save(existingInvitation);
        verify(invitationMapper).toResponseDto(existingInvitation);
    }

    @Test
    void acceptInvitation_WithValidToken_ShouldSucceed() {
        // Arrange
        Invitation invitation = new Invitation(project, regularUser.getEmail(), ProjectRoles.MEMBER);
        TestEntityUtils.setId(invitation, UUID.randomUUID());

        String token = invitation.getInviteToken().toString();

        when(invitationRepository.findByInviteToken(any(UUID.class)))
                .thenReturn(Optional.of(invitation));
        when(collaboratorService.addCollaborator(project, regularUser, ProjectRoles.MEMBER))
                .thenReturn(null);
        when(invitationRepository.save(any(Invitation.class))).thenReturn(invitation);

        // Act
        var response = invitationService.acceptInvitation(token, regularUser);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(project.getId().toString(), response.getProjectId());
        assertEquals(project.getTitle(), response.getProjectName());
        verify(collaboratorService).addCollaborator(project, regularUser, ProjectRoles.MEMBER);
        verify(invitationRepository).save(invitation);
        assertEquals(InvitationStatus.USED, invitation.getStatus());
        assertNotNull(invitation.getUsedAt());
    }

    @Test
    void acceptInvitation_WithExpiredToken_ShouldThrowException() {
        // Arrange
        Invitation invitation = new Invitation(project, regularUser.getEmail(), ProjectRoles.MEMBER);
        TestEntityUtils.setId(invitation, UUID.randomUUID());
        invitation.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired

        String token = invitation.getInviteToken().toString();

        when(invitationRepository.findByInviteToken(any(UUID.class)))
                .thenReturn(Optional.of(invitation));

        // Act & Assert
        assertThrows(InvitationException.class, () ->
                invitationService.acceptInvitation(token, regularUser)
        );

        verify(collaboratorService, never()).addCollaborator(any(), any(), any());
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void acceptInvitation_WithEmailMismatch_ShouldThrowException() {
        // Arrange
        Invitation invitation = new Invitation(project, "different@test.com", ProjectRoles.MEMBER);
        TestEntityUtils.setId(invitation, UUID.randomUUID());

        String token = invitation.getInviteToken().toString();

        when(invitationRepository.findByInviteToken(any(UUID.class)))
                .thenReturn(Optional.of(invitation));

        // Act & Assert
        InvitationException exception = assertThrows(InvitationException.class, () ->
                invitationService.acceptInvitation(token, regularUser)
        );

        assertTrue(exception.getMessage().contains("different email"));
        verify(collaboratorService, never()).addCollaborator(any(), any(), any());
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void acceptInvitation_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid-uuid";

        // Act & Assert
        assertThrows(InvitationException.class, () ->
                invitationService.acceptInvitation(invalidToken, regularUser)
        );

        verify(invitationRepository, never()).findByInviteToken(any());
    }

    @Test
    void acceptInvitation_WithNonExistentToken_ShouldThrowException() {
        // Arrange
        UUID token = UUID.randomUUID();
        when(invitationRepository.findByInviteToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvitationException.class, () ->
                invitationService.acceptInvitation(token.toString(), regularUser)
        );
    }
}