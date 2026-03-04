package de.upteams.tasktracker.user.controller.interfaces;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.user.dto.request.UserCreateDto;
import de.upteams.tasktracker.user.dto.response.UserCreateResponseDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Registration", description = "Endpoints for user registration and email confirmation")
public interface RegisterControllerApi {


    @Operation(summary = "Register new user", description = "Creates a new user account and sends a confirmation email. If user already exists but is unconfirmed, resends the confirmation email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserCreateResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "123e4567-e89b-12d3-a456-426614174000",
                                      "email": "tes_dev@upteams.de",
                                      "role": "ROLE_USER",
                                      "confirmationResent": false
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "409", description = "User already exists and confirmed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": 1627660173000,
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "User already exists",
                                      "path": "/api/v1/users/register"
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "email", "error": "must be a well-formed email address" },
                                      { "field": "password", "error": "must not be blank" }
                                    ]
                                    """))
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    UserCreateResponseDto register(
            @RequestBody
            @Valid
            UserCreateDto registerUser
    );

    @Operation(summary = "Confirm user registration", description = "Confirms a user's registration using a confirmation code sent via email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User confirmed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "tes_dev@upteams.de",
                                      "role": "ROLE_USER",
                                      "confirmationStatus": "CONFIRMED"
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "404", description = "Confirmation code not found or expired",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": 1627660173000,
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Confirmation code is invalid or expired",
                                      "path": "/api/v1/users/confirm/abcdef123456"
                                    }
                                    """))
            )
    })
    @GetMapping("/confirm/{code}")
    UserResponseDto confirmRegistration(
            @PathVariable
            String code
    );
}
