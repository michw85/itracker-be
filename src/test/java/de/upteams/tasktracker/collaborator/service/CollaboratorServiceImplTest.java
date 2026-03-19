package de.upteams.tasktracker.collaborator.service;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.impl.CollaboratorServiceImpl;
import de.upteams.tasktracker.project.entity.Project;
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

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CollaboratorServiceImplTest {

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @InjectMocks
    private CollaboratorServiceImpl collaboratorService;

    private UUID projectId;
    private UUID userId;
    private UUID collaboratorId;
    private AppUser testUser;
    private Project testProject;
    private Collaborator testCollaborator;

    @BeforeEach
    void setUp() throws Exception {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
        collaboratorId = UUID.randomUUID();

        // Create a test user
        testUser = new AppUser("password", "test@example.com");
        setId(testUser, "id", userId);

        // Let's create a test project
        testProject = new Project("Test Project", "Test Description", testUser);
        setId(testProject, "id", projectId);

        // Creating a test collaborator
        testCollaborator = new Collaborator();
        testCollaborator.setAppUser(testUser);
        testCollaborator.setProject(testProject);
        testCollaborator.getProjectRolesSet().add(ProjectRoles.MEMBER);
        setId(testCollaborator, "id", collaboratorId);
    }

    private void setId(Object object, String fieldName, UUID id) throws Exception {
        // We search for a field in all parent classes
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

    @Test
    void getUserRoleInProject_WhenCollaboratorExists_ShouldReturnRole() {
        when(collaboratorRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(testCollaborator));

        Optional<ProjectRoles> result = collaboratorService.getUserRoleInProject(projectId, userId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(ProjectRoles.MEMBER);
    }

    @Test
    void getUserRoleInProject_WhenCollaboratorNotExists_ShouldReturnEmpty() {
        when(collaboratorRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.empty());

        Optional<ProjectRoles> result = collaboratorService.getUserRoleInProject(projectId, userId);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserRoleInProject_WithMultipleRoles_ShouldReturnHighestPriority() {
        testCollaborator.getProjectRolesSet().clear();
        testCollaborator.getProjectRolesSet().add(ProjectRoles.VIEWER);
        testCollaborator.getProjectRolesSet().add(ProjectRoles.ADMIN);

        when(collaboratorRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(testCollaborator));

        Optional<ProjectRoles> result = collaboratorService.getUserRoleInProject(projectId, userId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(ProjectRoles.ADMIN); // ADMIN выше чем VIEWER
    }

    @Test
    void getProjectsByUser_ShouldReturnProjects() {
        List<Collaborator> collaborators = Arrays.asList(testCollaborator);
        when(collaboratorRepository.findByAppUser(testUser)).thenReturn(collaborators);

        List<Project> result = collaboratorService.getProjectsByUser(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(projectId);
    }

    @Test
    void isUserInProject_WhenUserIsCollaborator_ShouldReturnTrue() {
        when(collaboratorRepository.findCollaborator(testUser, testProject))
                .thenReturn(Optional.of(testCollaborator));

        boolean result = collaboratorService.isUserInProject(testUser, testProject);

        assertThat(result).isTrue();
    }

    @Test
    void hasUserPermission_WhenUserHasRole_ShouldReturnTrue() {
        when(collaboratorRepository.findCollaborator(testUser, testProject))
                .thenReturn(Optional.of(testCollaborator));

        boolean result = collaboratorService.hasUserPermission(
                testUser, testProject, ProjectRoles.MEMBER
        );

        assertThat(result).isTrue();
    }

    @Test
    void hasUserPermission_WhenUserDoesNotHaveRole_ShouldReturnFalse() {
        when(collaboratorRepository.findCollaborator(testUser, testProject))
                .thenReturn(Optional.of(testCollaborator));

        boolean result = collaboratorService.hasUserPermission(
                testUser, testProject, ProjectRoles.OWNER
        );

        assertThat(result).isFalse();
    }

    @Test
    void addCollaborator_ShouldCreateNewCollaborator() {
        when(collaboratorRepository.findCollaborator(testUser, testProject))
                .thenReturn(Optional.empty());
        when(collaboratorRepository.save(any(Collaborator.class)))
                .thenAnswer(invocation -> {
                    Collaborator saved = invocation.getArgument(0);
                    setId(saved, "id", collaboratorId);
                    return saved;
                });

        Collaborator result = collaboratorService.addCollaborator(
                testProject, testUser, ProjectRoles.MEMBER
        );

        assertThat(result).isNotNull();
        assertThat(result.getAppUser()).isEqualTo(testUser);
        assertThat(result.getProject()).isEqualTo(testProject);
        assertThat(result.getProjectRolesSet()).contains(ProjectRoles.MEMBER);
        verify(collaboratorRepository).save(any(Collaborator.class));
    }

    @Test
    void addCollaborator_WhenExists_ShouldReturnExisting() {
        when(collaboratorRepository.findCollaborator(testUser, testProject))
                .thenReturn(Optional.of(testCollaborator));

        Collaborator result = collaboratorService.addCollaborator(
                testProject, testUser, ProjectRoles.MEMBER
        );

        assertThat(result).isEqualTo(testCollaborator);
        verify(collaboratorRepository, never()).save(any(Collaborator.class));
    }
}