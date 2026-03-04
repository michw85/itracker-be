package de.upteams.tasktracker.user.util;

import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppUserMapper Test")
class AppUserMapperTest {

    private final AppUserMapper underTest = Mappers.getMapper(AppUserMapper.class);

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
