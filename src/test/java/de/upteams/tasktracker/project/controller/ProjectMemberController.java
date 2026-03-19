package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.invitation.repository.InvitationRepository;
import de.upteams.tasktracker.invitation.service.InvitationService;
import de.upteams.tasktracker.project.dto.MemberResponseDto;
import de.upteams.tasktracker.project.dto.ProjectRoleResponse;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectMemberControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private InvitationService invitationService;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private ProjectMemberController controller;

    private UUID projectId;
    private UUID userId;
    private AppUser testUser;
    private AuthUserDetails authUserDetails;
    private Project testProject;

    @BeforeEach
    void setUp() throws Exception {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testUser = new AppUser("password", "test@example.com");
        setId(testUser, userId);

        authUserDetails = new AuthUserDetails(testUser);

        testProject = new Project("Test Project", "Test Description", testUser);
        setId(testProject, projectId);
    }

    private void setId(BaseEntity entity, UUID id) throws Exception {
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    void getMyRole_WhenUserIsMember_ShouldReturnRole() {
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenReturn(Optional.of(ProjectRoles.MEMBER));

        ResponseEntity<ProjectRoleResponse> response = controller.getMyRole(projectId, authUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRole()).isEqualTo("MEMBER");
    }

    @Test
    void getMyRole_WhenUserIsOwner_ShouldReturnOwnerRole() {
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenReturn(Optional.empty());

        when(projectService.getOrTrow(projectId.toString())).thenReturn(testProject);

        ResponseEntity<ProjectRoleResponse> response = controller.getMyRole(projectId, authUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRole()).isEqualTo("OWNER");
    }

    @Test
    void getMyRole_WhenUserNotMember_ShouldReturn403() throws Exception {
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenReturn(Optional.empty());

        Project projectWithoutOwner = new Project("Test Project", "Test Description", null);
        setId(projectWithoutOwner, projectId);

        when(projectService.getOrTrow(projectId.toString())).thenReturn(projectWithoutOwner);

        ResponseEntity<ProjectRoleResponse> response = controller.getMyRole(projectId, authUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getMyRole_WhenException_ShouldReturn500() {
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<ProjectRoleResponse> response = controller.getMyRole(projectId, authUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getProjectMembers_WhenUserHasAccess_ShouldReturnMembers() {
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenReturn(Optional.of(ProjectRoles.ADMIN));

        List<MemberResponseDto> members = Arrays.asList(
                MemberResponseDto.builder()
                        .id(UUID.randomUUID())
                        .email("member1@example.com")
                        .role(ProjectRoles.MEMBER)
                        .status("ACTIVE")
                        .build()
        );

        when(invitationService.getProjectMembers(projectId)).thenReturn(members);

        // Убрали третий параметр new HashMap<>()
        ResponseEntity<List<MemberResponseDto>> response =
                controller.getProjectMembers(projectId, authUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getEmail()).isEqualTo("member1@example.com");
    }

    @Test
    void getProjectMembers_WhenUserNoAccess_ShouldReturn403() {
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenReturn(Optional.empty());

        // Убрали третий параметр new HashMap<>()
        ResponseEntity<List<MemberResponseDto>> response =
                controller.getProjectMembers(projectId, authUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(invitationService, never()).getProjectMembers(any());
    }

    @Test
    void debugProjectCheck_ShouldReturnProjectInfo() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenReturn(Optional.of(ProjectRoles.OWNER));
        when(collaboratorRepository.findByProjectId(projectId)).thenReturn(new ArrayList<>());
        when(invitationRepository.findByProjectId(projectId)).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response =
                controller.debugProjectCheck(projectId, authUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("projectId");
        assertThat(response.getBody()).containsKey("userId");
        assertThat(response.getBody().get("projectId")).isEqualTo(projectId);
    }
}