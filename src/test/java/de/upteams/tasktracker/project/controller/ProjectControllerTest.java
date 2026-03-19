package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.dto.response.ProjectSummaryDto;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectController projectController;

    private AppUser testUser;
    private AuthUserDetails authUserDetails;
    private ProjectCreateDto createDto;
    private ProjectResponseDto responseDto;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();

        // Create a test user
        testUser = new AppUser();
        // Using reflection to set the ID
        try {
            var idField = AppUser.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, projectId);

            var emailField = AppUser.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(testUser, "test@example.com");
        } catch (Exception e) {
            e.printStackTrace();
        }

        authUserDetails = new AuthUserDetails(testUser);

        // ProjectCreateDto - record, use the constructor
        createDto = new ProjectCreateDto("Test Project", "Test Description");

        // ProjectResponseDto - record, use the constructor
        responseDto = new ProjectResponseDto(
                projectId,
                "Test Project",
                "Test Description",
                projectId
        );
    }

    @Test
    void save_ShouldReturnCreatedProject() {
        when(projectService.save(any(ProjectCreateDto.class), any(AppUser.class)))
                .thenReturn(responseDto);

        ProjectResponseDto result = projectController.save(createDto, authUserDetails);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(projectId);
        assertThat(result.title()).isEqualTo("Test Project");
        verify(projectService).save(createDto, testUser);
    }

    @Test
    void getById_ShouldReturnProject() {
        String id = projectId.toString();
        when(projectService.getById(id)).thenReturn(responseDto);

        ProjectResponseDto result = projectController.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(projectId);
        verify(projectService).getById(id);
    }

    @Test
    void getUserProjects_ShouldReturnProjectsForUser() throws Exception {
        List<ProjectResponseDto> projects = Arrays.asList(responseDto);

        // Create a mock for a method that returns a List<Project>
        when(projectService.getUserProjects(any(AppUser.class))).thenReturn(List.of());

        // Using reflection to call the method
        ResponseEntity<List<ProjectResponseDto>> response =
                projectController.getUserProjects(authUserDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}