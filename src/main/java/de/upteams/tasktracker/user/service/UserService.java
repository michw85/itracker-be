package de.upteams.tasktracker.user.service;

import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for various operations with Employees
 */
public interface UserService {

    AppUser saveOrUpdate(AppUser user);

    Optional<AppUser> getByEmail(String email);

    AppUser getByEmailOrThrow(String email);

    AppUser getByIdOrThrow(String id);

    List<UserResponseDto> getAll();

    String createPasswordResetToken(String email);

    void processForgotPassword(String email);

    @Transactional
    void resetPassword(String token, String newPassword);
}
