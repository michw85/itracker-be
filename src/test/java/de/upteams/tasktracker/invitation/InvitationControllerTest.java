package de.upteams.tasktracker.invitation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.invitation.controller.InvitationController;
import de.upteams.tasktracker.invitation.dto.request.InvitationRequestDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationAcceptResponseDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationResponseDto;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import de.upteams.tasktracker.invitation.service.InvitationService;
import de.upteams.tasktracker.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for InvitationController with mock user authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvitationService invitationService;

    @MockitoBean
    private UserService userService;

    private UUID projectId;
    private InvitationRequestDto requestDto;
    private InvitationResponseDto responseDto;
    private InvitationAcceptResponseDto acceptResponseDto;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();

        // Setup request DTO
        requestDto = new InvitationRequestDto();
        requestDto.setEmail("invited@test.com");
        requestDto.setRole(ProjectRoles.MEMBER);

        // Setup response DTO
        responseDto = InvitationResponseDto.builder()
                .id(UUID.randomUUID())
                .email(requestDto.getEmail())
                .role(ProjectRoles.MEMBER)
                .status(InvitationStatus.PENDING)
                .inviteToken(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusHours(72))
                .projectId(projectId)
                .projectName("Test Project")
                .build();

        // Setup accept response DTO
        acceptResponseDto = InvitationAcceptResponseDto.builder()
                .message("Successfully joined the project")
                .success(true)
                .projectId(projectId.toString())
                .projectName("Test Project")
                .build();
    }

//    @Test
//    @WithMockUser(username = "owner@test.com", roles = "USER")
//    void createInvitation_ForUnregisteredUser_ShouldReturn202() throws Exception {
//        // Arrange
//        when(userService.getByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
//        when(invitationService.createOrUpdateInvitation(
//                eq(projectId.toString()),
//                any(InvitationRequestDto.class),
//                any()
//        )).thenReturn(responseDto);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/projects/{projectId}/invitations", projectId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isAccepted())
//                .andExpect(jsonPath("$.email").value(requestDto.getEmail()))
//                .andExpect(jsonPath("$.role").value(ProjectRoles.MEMBER.name()))
//                .andExpect(jsonPath("$.status").value(InvitationStatus.PENDING.name()))
//                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
//                .andExpect(jsonPath("$.projectName").value("Test Project"));
//    }
//
//    @Test
//    @WithMockUser(username = "owner@test.com", roles = "USER")
//    void createInvitation_ForRegisteredUser_ShouldReturn200() throws Exception {
//        // Arrange
//        when(userService.getByEmail(requestDto.getEmail())).thenReturn(Optional.of(new de.upteams.tasktracker.user.entity.AppUser()));
//        when(invitationService.createOrUpdateInvitation(
//                eq(projectId.toString()),
//                any(InvitationRequestDto.class),
//                any()
//        )).thenReturn(responseDto);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/projects/{projectId}/invitations", projectId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.email").value(requestDto.getEmail()))
//                .andExpect(jsonPath("$.role").value(ProjectRoles.MEMBER.name()))
//                .andExpect(jsonPath("$.status").value(InvitationStatus.PENDING.name()));
//    }
//
//    @Test
//    @WithMockUser(username = "user@test.com", roles = "USER")
//    void acceptInvitation_WithValidToken_ShouldReturn200() throws Exception {
//        // Arrange
//        String token = UUID.randomUUID().toString();
//
//        when(invitationService.acceptInvitation(eq(token), any()))
//                .thenReturn(acceptResponseDto);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/invitations/accept")
//                        .param("token", token))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Successfully joined the project"))
//                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
//                .andExpect(jsonPath("$.projectName").value("Test Project"));
//    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void createInvitation_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Arrange
        InvitationRequestDto invalidRequest = new InvitationRequestDto();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setRole(ProjectRoles.MEMBER);

        // Act & Assert
        mockMvc.perform(post("/api/v1/projects/{projectId}/invitations", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "USER")
    void createInvitation_WithNullRole_ShouldReturn400() throws Exception {
        // Arrange
        InvitationRequestDto invalidRequest = new InvitationRequestDto();
        invalidRequest.setEmail("test@test.com");
        invalidRequest.setRole(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/projects/{projectId}/invitations", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}