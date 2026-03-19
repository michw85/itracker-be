package de.upteams.tasktracker.project.service;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.dto.response.ProjectSummaryDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.impl.ProjectServiceImpl;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.user.entity.AppUser;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository repository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID projectId;
    private UUID userId;
    private AppUser testUser;
    private Project testProject;
    private ProjectCreateDto createDto;
    private ProjectResponseDto responseDto;

    @BeforeEach
    void setUp() throws Exception {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Create a test user
        testUser = new AppUser("password", "test@example.com");
        setId(testUser, "id", userId);

        // Let's create a test project
        testProject = new Project("Test Project", "Test Description", testUser);
        setId(testProject, "id", projectId);

        // Create a DTO using the record constructor
        createDto = new ProjectCreateDto("Test Project", "Test Description");

        // Create a ResponseDto using the record constructor
        responseDto = new ProjectResponseDto(
                projectId,
                "Test Project",
                "Test Description",
                userId
        );
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
    void save_ShouldSaveAndReturnProject() {
        when(projectMapper.mapDtoToEntity(createDto)).thenReturn(testProject);
        when(repository.save(any(Project.class))).thenReturn(testProject);
        when(projectMapper.toResponseDto(testProject)).thenReturn(responseDto);

        ProjectResponseDto result = projectService.save(createDto, testUser);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(projectId);
        assertThat(result.title()).isEqualTo("Test Project");
        verify(repository).save(testProject);
    }

    @Test
    void getById_ShouldReturnProject() {
        String id = projectId.toString();
        when(repository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectMapper.toResponseDto(testProject)).thenReturn(responseDto);

        ProjectResponseDto result = projectService.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(projectId);
    }

    @Test
    void getOrTrow_WhenProjectNotExists_ShouldThrowException() {
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () ->
                projectService.getOrTrow(projectId.toString())
        );
    }

    @Test
    void getUserRoleInProject_ShouldReturnRole() {
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenReturn(Optional.of(ProjectRoles.ADMIN));

        String role = projectService.getUserRoleInProject(projectId, userId);

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void getUserRoleInProject_WhenNotMember_ShouldThrowException() {
        when(collaboratorService.getUserRoleInProject(projectId, userId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                projectService.getUserRoleInProject(projectId, userId)
        );
    }

    @Test
    void getProjectSummaries_ShouldReturnSummaries() {
        List<Project> userProjects = Arrays.asList(testProject);
        when(repository.findByOwner(testUser)).thenReturn(userProjects);
        when(collaboratorService.getProjectsByUser(testUser)).thenReturn(Arrays.asList());

        when(taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.ACTIVE))
                .thenReturn(5);
        when(collaboratorRepository.countByProjectId(projectId)).thenReturn(2);

        List<ProjectSummaryDto> summaries = projectService.getProjectSummaries(testUser);

        assertThat(summaries).hasSize(1);
        ProjectSummaryDto summary = summaries.get(0);
        assertThat(summary.getId()).isEqualTo(projectId);
        assertThat(summary.getActiveTasksCount()).isEqualTo(5);
        assertThat(summary.getExecutorsCount()).isEqualTo(3); // 2 collaborators + 1 owner
    }

    @Test
    void delete_ShouldCallRepository() {
        String id = projectId.toString();
        doNothing().when(repository).deleteById(projectId);

        projectService.delete(id);

        verify(repository).deleteById(projectId);
    }
}