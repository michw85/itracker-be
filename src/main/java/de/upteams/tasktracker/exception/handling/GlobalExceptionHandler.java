package de.upteams.tasktracker.exception.handling;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global handler for all exceptions that threw in the application
 */
@RestControllerAdvice
@Hidden
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<ErrorResponseDto> handleRestApiException(
            RestApiException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                LocalDateTime.now(),
                ex.getHttpStatus().value(),
                ex.getHttpStatus().getReasonPhrase(),
                ex.getMessage(),
                List.of(),
                request.getRequestURI()
        );
        log.error("RestApi exception caught: {}.", ExceptionUtils.getMessage(ex), ex);
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, List<String>> fieldErrorsMap = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrorsMap.computeIfAbsent(fieldError.getField(), key -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }

        List<ValidationErrorDto> validationErrors = fieldErrorsMap.entrySet().stream()
                .map(entry -> new ValidationErrorDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for one or more fields",
                validationErrors,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

//    *
//     * Delegate any AuthenticationException (401 Unauthorized)
//     * to the RestAuthenticationEntryPoint, so it renders your JSON.
//
//    @ExceptionHandler(AuthenticationException.class)
//    public void handleAuthenticationException(
//            AuthenticationException ex,
//            HttpServletRequest request,
//            HttpServletResponse response
//    ) throws IOException {
//        log.warn("Authentication failure: {}", ex.getMessage());
//        new RestAuthenticationEntryPoint().commence(request, response, ex);
//    }
//
//    *
//     * Delegate any AccessDeniedException (403 Forbidden)
//     * to the CustomAccessDeniedHandler.
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public void handleAccessDeniedException(
//            AccessDeniedException ex,
//            HttpServletRequest request,
//            HttpServletResponse response
//    ) throws IOException {
//        log.warn("Access denied: {}", ex.getMessage());
//        new CustomAccessDeniedHandler().handle(request, response, ex);
//    }
//
//    *
//     * Fallback for any other exceptions.
//     * Logs the error and returns 500 with a generic message.
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponseDto> handleUncaughtExceptions(
//            Exception ex,
//            HttpServletRequest request
//    ) {
//        log.error("Unhandled exception caught: {}.", ExceptionUtils.getMessage(ex), ex);
//
//        ErrorResponseDto errorResponse = new ErrorResponseDto(
//                LocalDateTime.now(),
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
//                "Internal server error",
//                List.of(),
//                request.getRequestURI()
//        );
//
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(errorResponse);
//    }
}
