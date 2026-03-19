package de.upteams.tasktracker.invitation;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.invitation.dto.request.InvitationRequestDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationAcceptResponseDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationResponseDto;
import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import de.upteams.tasktracker.invitation.mapper.InvitationMapper;
import de.upteams.tasktracker.invitation.repository.InvitationRepository;
import de.upteams.tasktracker.invitation.service.InvitationService;
import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import de.upteams.tasktracker.utils.BaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private EmailService emailService;

    @Mock
    private InvitationMapper invitationMapper;

    @InjectMocks
    private InvitationService invitationService;

    private UUID projectId;
    private UUID userId;
    private UUID inviterId;
    private UUID invitationId;
    private UUID tokenUuid;
    private Project testProject;
    private AppUser testUser;
    private AppUser inviter;
    private Invitation testInvitation;
    private InvitationRequestDto requestDto;
    private InvitationResponseDto responseDto;

    @BeforeEach
    void setUp() throws Exception {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
        inviterId = UUID.randomUUID();
        invitationId = UUID.randomUUID();
        tokenUuid = UUID.randomUUID();

        // Let's create a test project
        testProject = new Project("Test Project", "Test Description", null);
        setIdInHierarchy(testProject, "id", projectId);

        // Create a test user (invited)
        testUser = new AppUser("password", "user@test.com");
        setIdInHierarchy(testUser, "id", userId);

        // Create an inviting user
        inviter = new AppUser("password", "inviter@test.com");
        setIdInHierarchy(inviter, "id", inviterId);
        // Setting the project owner
        setField(testProject, "owner", inviter);

        // Create a test invitation
        testInvitation = new Invitation(testProject, "user@test.com", ProjectRoles.MEMBER);
        setIdInHierarchy(testInvitation, "id", invitationId);
        setField(testInvitation, "inviteToken", tokenUuid);
        setField(testInvitation, "status", InvitationStatus.PENDING);
        setField(testInvitation, "expiresAt", LocalDateTime.now().plusHours(72));

        // Creating a DTO for a request - using a default constructor and reflection
        requestDto = new InvitationRequestDto();
        setField(requestDto, "email", "user@test.com");
        setField(requestDto, "role", ProjectRoles.MEMBER);

        // Create a DTO for the response
        responseDto = InvitationResponseDto.builder()
                .id(invitationId)
                .email("user@test.com")
                .role(ProjectRoles.MEMBER)
                .status(InvitationStatus.PENDING)
                .inviteToken(tokenUuid)
                .expiresAt(LocalDateTime.now().plusHours(72))
                .projectId(projectId)
                .projectName("Test Project")
                .build();

        // We check that the IDs are set correctly.
        assertThat(getIdFromEntity(testProject)).isEqualTo(projectId);
        assertThat(getIdFromEntity(testUser)).isEqualTo(userId);
        assertThat(getIdFromEntity(testInvitation)).isEqualTo(invitationId);
    }

    private void setIdInHierarchy(Object object, String fieldName, UUID id) throws Exception {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, id);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Could not find field " + fieldName + " in " + object.getClass());
    }

    private UUID getIdFromEntity(Object entity) throws Exception {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("id");
                field.setAccessible(true);
                return (UUID) field.get(entity);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Could not find field id in " + entity.getClass());
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void createInvitation_ForUnregisteredUser_ShouldSucceed() {
        when(projectService.getOrTrow(projectId.toString())).thenReturn(testProject);
        when(userService.getByEmail("user@test.com")).thenReturn(Optional.empty());
        when(invitationRepository.findPendingByProjectAndEmail(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
        when(invitationMapper.toResponseDto(any(Invitation.class))).thenReturn(responseDto);

        InvitationResponseDto result = invitationService.createOrUpdateInvitation(
                projectId.toString(), requestDto, inviter);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@test.com");
        verify(emailService).sendRegistrationInvitationEmail(any(), any(), any());
    }

    @Test
    void createInvitation_ForRegisteredUser_ShouldSucceed() {
        when(projectService.getOrTrow(projectId.toString())).thenReturn(testProject);
        when(userService.getByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(collaboratorService.isUserInProject(testUser, testProject)).thenReturn(false);
        when(invitationRepository.findPendingByProjectAndEmail(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
        when(invitationMapper.toResponseDto(any(Invitation.class))).thenReturn(responseDto);

        InvitationResponseDto result = invitationService.createOrUpdateInvitation(
                projectId.toString(), requestDto, inviter);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@test.com");
        verify(emailService).sendProjectInvitationEmail(any(), any(), any());
    }

    @Test
    void createInvitation_WhenInvitationExists_ShouldUpdateExpiration() {
        LocalDateTime oldExpiration = testInvitation.getExpiresAt();

        when(projectService.getOrTrow(projectId.toString())).thenReturn(testProject);
        when(userService.getByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(collaboratorService.isUserInProject(testUser, testProject)).thenReturn(false);
        when(invitationRepository.findPendingByProjectAndEmail(any(), any(), any()))
                .thenReturn(Optional.of(testInvitation));
        when(invitationRepository.save(any(Invitation.class))).thenReturn(testInvitation);
        when(invitationMapper.toResponseDto(any(Invitation.class))).thenReturn(responseDto);

        InvitationResponseDto result = invitationService.createOrUpdateInvitation(
                projectId.toString(), requestDto, inviter);

        assertThat(result).isNotNull();
        assertThat(testInvitation.getExpiresAt()).isAfter(oldExpiration);
    }

    @Test
    void createInvitation_WhenUserAlreadyMember_ShouldThrowException() {
        when(projectService.getOrTrow(projectId.toString())).thenReturn(testProject);
        when(userService.getByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(collaboratorService.isUserInProject(testUser, testProject)).thenReturn(true);

        assertThrows(Exception.class, () ->
                invitationService.createOrUpdateInvitation(projectId.toString(), requestDto, inviter)
        );
    }

    @Test
    void acceptInvitation_WithValidToken_ShouldSucceed() throws Exception {
        UUID token = UUID.randomUUID();
        setField(testInvitation, "inviteToken", token);
        setField(testInvitation, "status", InvitationStatus.PENDING);
        setField(testInvitation, "expiresAt", LocalDateTime.now().plusHours(72));

        // Make sure the project has an ID
        UUID projectIdFromEntity = getIdFromEntity(testProject);
        assertThat(projectIdFromEntity).isNotNull();
        assertThat(projectIdFromEntity).isEqualTo(projectId);

        when(invitationRepository.findByInviteToken(token)).thenReturn(Optional.of(testInvitation));
        // We explicitly indicate which method we are calling - with one role
        doReturn(null).when(collaboratorService).addCollaborator(
                any(Project.class), any(AppUser.class), any(ProjectRoles.class)
        );

        InvitationAcceptResponseDto result = invitationService.acceptInvitation(
                token.toString(), testUser);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Successfully joined the project");
        assertThat(result.getProjectId()).isEqualTo(projectId.toString());
    }

    @Test
    void acceptInvitation_WithExpiredToken_ShouldThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        setField(testInvitation, "inviteToken", token);
        setField(testInvitation, "status", InvitationStatus.PENDING);
        setField(testInvitation, "expiresAt", LocalDateTime.now().minusHours(1));

        when(invitationRepository.findByInviteToken(token)).thenReturn(Optional.of(testInvitation));

        assertThrows(Exception.class, () ->
                invitationService.acceptInvitation(token.toString(), testUser)
        );
    }

    @Test
    void acceptInvitation_WithInvalidToken_ShouldThrowException() {
        when(invitationRepository.findByInviteToken(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
                invitationService.acceptInvitation(UUID.randomUUID().toString(), testUser)
        );
    }

    @Test
    void acceptInvitation_WithEmailMismatch_ShouldThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        setField(testInvitation, "inviteToken", token);
        setField(testInvitation, "email", "different@test.com");

        when(invitationRepository.findByInviteToken(token)).thenReturn(Optional.of(testInvitation));

        assertThrows(Exception.class, () ->
                invitationService.acceptInvitation(token.toString(), testUser)
        );
    }
}