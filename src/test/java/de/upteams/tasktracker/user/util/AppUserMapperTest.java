package de.upteams.tasktracker.user.util;

import de.upteams.tasktracker.user.dto.EmployeeDto;
import de.upteams.tasktracker.user.dto.response.UserCreateResponseDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppUserMapperImpl.class})
@DisplayName("AppUserMapper Test")
class AppUserMapperTest {

    private final AppUserMapper underTest = Mappers.getMapper(AppUserMapper.class);

    @Autowired
    private AppUserMapper mapper;

    private AppUser user;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();

        // Создаем пользователя через конструктор
        user = new AppUser("encodedPassword", "test@example.com", "Test User");

        // Устанавливаем ID через рефлексию (так как setId нет)
        setId(user, userId);

        // Устанавливаем остальные поля через сеттеры (они есть)
        user.setPosition("Developer");
        user.setDepartment("Engineering");
        user.setBio("Test bio");
        user.setAvatarUrl("http://example.com/avatar.jpg");
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        user.setRole(Role.ROLE_USER);
    }

    /**
     * Вспомогательный метод для установки ID через рефлексию
     */
    private void setId(AppUser user, UUID id) throws Exception {
        Field idField = user.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    @Test
    void mapEntityToDto_ShouldMapAllFields() {
        // When
        UserResponseDto dto = mapper.mapEntityToDto(user);

        // Then
        assertNotNull(dto);
        assertEquals(userId.toString(), dto.id());
        assertEquals("test@example.com", dto.email());
        assertEquals("Test User", dto.displayName());
        assertEquals("Developer", dto.position());
        assertEquals("Engineering", dto.department());
        assertEquals("Test bio", dto.bio());
        assertEquals("http://example.com/avatar.jpg", dto.avatarUrl());
        assertEquals("ROLE_USER", dto.role());
        assertEquals(ConfirmationStatus.CONFIRMED, dto.confirmationStatus());
    }

    @Test
    void mapEntityToDto_WithNullFields_ShouldNotThrowException() {
        // Given
        AppUser nullUser = new AppUser("encodedPassword", "test@example.com", null);
        try {
            setId(nullUser, userId);
        } catch (Exception e) {
            fail("Failed to set ID: " + e.getMessage());
        }

        // When
        UserResponseDto dto = mapper.mapEntityToDto(nullUser);

        // Then
        assertNotNull(dto);
        assertEquals(userId.toString(), dto.id());
        assertEquals("test@example.com", dto.email());
        assertNull(dto.displayName());
        assertNull(dto.position());
        assertNull(dto.department());
        assertNull(dto.bio());
        assertNull(dto.avatarUrl());
    }

    @Test
    void mapEntityToDto_WithNullEntity_ShouldReturnNull() {
        // When
        UserResponseDto dto = mapper.mapEntityToDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    void mapEntityToCreateResponseDto_ShouldMapAllFields() {
        // When
        UserCreateResponseDto dto = mapper.mapEntityToCreateResponseDto(user);

        // Then
        assertNotNull(dto);
        assertEquals(userId.toString(), dto.id());
        assertEquals("test@example.com", dto.email());
        assertEquals("Test User", dto.displayName());
        assertEquals("ROLE_USER", dto.role());
        assertFalse(dto.confirmationResent());
    }

    @Test
    void mapEntityToCreateResponseDto_WithNullFields_ShouldNotThrowException() {
        // Given
        AppUser nullUser = new AppUser("encodedPassword", "test@example.com", null);
        try {
            setId(nullUser, userId);
        } catch (Exception e) {
            fail("Failed to set ID: " + e.getMessage());
        }

        // When
        UserCreateResponseDto dto = mapper.mapEntityToCreateResponseDto(nullUser);

        // Then
        assertNotNull(dto);
        assertEquals(userId.toString(), dto.id());
        assertEquals("test@example.com", dto.email());
        assertNull(dto.displayName());
        assertEquals("ROLE_USER", dto.role());
        assertFalse(dto.confirmationResent());
    }


    @Test
    void mapToEmployeeDto_ShouldMapAllFields() {
        // When
        EmployeeDto dto = mapper.mapToEmployeeDto(user);

        // Then
        assertNotNull(dto);
        assertEquals(userId.toString(), dto.id());
        assertEquals("Test User", dto.displayName());
        assertEquals("test@example.com", dto.email());
        assertEquals("Developer", dto.position());
        assertEquals("http://example.com/avatar.jpg", dto.avatarUrl());  
    }

    @Test
    void mapToEmployeeDto_WithNullFields_ShouldNotThrowException() {
        // Given
        AppUser nullUser = new AppUser("encodedPassword", "test@example.com", null);
        try {
            setId(nullUser, userId);
        } catch (Exception e) {
            fail("Failed to set ID: " + e.getMessage());
        }

        // When
        EmployeeDto dto = mapper.mapToEmployeeDto(nullUser);

        // Then
        assertNotNull(dto);
        assertEquals(userId.toString(), dto.id());
        assertEquals("test@example.com", dto.email());
        assertNull(dto.displayName());
        assertNull(dto.position());
        assertNull(dto.avatarUrl());
    }

    @Nested
    @DisplayName("mapEntityToDto Tests")
    class MapEntityToDto {

        @Test
        @DisplayName("Should map AppUser to UserResponseDto correctly")
        void shouldMapAppUserToUserResponseDtoCorrectly() {
            // Arrange
            AppUser appUser = new AppUser();
            appUser.setEmail("homer@simpsons.com");
            appUser.setRole(Role.ROLE_USER);
            appUser.setConfirmationStatus(ConfirmationStatus.UNCONFIRMED);

            // Act
            UserResponseDto result = underTest.mapEntityToDto(appUser);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo(appUser.getEmail());
            assertThat(result.role()).isEqualTo(appUser.getRole().name());
            assertThat(result.confirmationStatus()).isEqualTo(appUser.getConfirmationStatus());
        }

        @ParameterizedTest(name = "Should map AppUser with role {1} and confirmationStatus {2} correctly")
        @MethodSource("provideAppUserData")
        @DisplayName("Should map AppUser with all role and confirmationStatus combinations")
        void shouldMapAppUserWithAllRoleAndConfirmationStatusCombinations(
                String email, Role role, ConfirmationStatus confirmationStatus
        ) {
            // Arrange
            AppUser appUser = new AppUser();
            appUser.setEmail(email);
            appUser.setRole(role);
            appUser.setConfirmationStatus(confirmationStatus);

            // Act
            UserResponseDto result = underTest.mapEntityToDto(appUser);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo(email);
            assertThat(result.role()).isEqualTo(role.name());
            assertThat(result.confirmationStatus()).isEqualTo(confirmationStatus);
        }

        private static Stream<Arguments> provideAppUserData() {
            return Stream.of(
                    Arguments.of("homer@simpsons.com", Role.ROLE_USER, ConfirmationStatus.UNCONFIRMED),
                    Arguments.of("marge@simpsons.com", Role.ROLE_ADMIN, ConfirmationStatus.CONFIRMED),
                    Arguments.of("bart@simpsons.com", Role.ROLE_USER, ConfirmationStatus.BANNED),
                    Arguments.of("lisa@simpsons.com", Role.ROLE_ADMIN, ConfirmationStatus.BANNED),
                    Arguments.of("maggie@simpsons.com", Role.ROLE_USER, ConfirmationStatus.CONFIRMED)
            );
        }
    }

    @Nested
    @DisplayName("Negative tests for AppUserMapper")
    class NegativeTests {

        @Test
        @DisplayName("Should return null when AppUser is null")
        void shouldReturnNullWhenAppUserIsNull() {
            // Act
            UserResponseDto result = underTest.mapEntityToDto(null);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle AppUser with null fields")
        void shouldHandleAppUserWithNullFields() {
            // Arrange
            AppUser appUser = new AppUser();
            appUser.setEmail(null);
            appUser.setRole(null);
            appUser.setConfirmationStatus(null);

            // Act
            UserResponseDto result = underTest.mapEntityToDto(appUser);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.email()).isNull();
            assertThat(result.role()).isNull();
            assertThat(result.confirmationStatus()).isNull();
        }

        @Test
        @DisplayName("Should handle AppUser with empty email")
        void shouldHandleAppUserWithEmptyEmail() {
            // Arrange
            AppUser appUser = new AppUser();
            appUser.setEmail("");
            appUser.setRole(Role.ROLE_USER);
            appUser.setConfirmationStatus(ConfirmationStatus.CONFIRMED);

            // Act
            UserResponseDto result = underTest.mapEntityToDto(appUser);

            // Assert
            assertThat(result.email()).isEqualTo("");
        }
    }

}
