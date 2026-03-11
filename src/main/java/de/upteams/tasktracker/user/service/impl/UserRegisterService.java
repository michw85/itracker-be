package de.upteams.tasktracker.user.service.impl;

import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.mail.confirmation.code.ConfirmationCode;
import de.upteams.tasktracker.mail.confirmation.code.interfaces.ConfirmationService;
import de.upteams.tasktracker.user.dto.request.UserCreateDto;
import de.upteams.tasktracker.user.dto.response.UserCreateResponseDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.exception.UserAlreadyExistException;
import de.upteams.tasktracker.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.upteams.tasktracker.user.entity.ConfirmationStatus.CONFIRMED;
import static de.upteams.tasktracker.user.entity.ConfirmationStatus.UNCONFIRMED;

@Service
@RequiredArgsConstructor
public class UserRegisterService {

    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConfirmationService confirmationService;
    private final UserService userService;

    @Transactional
    public UserCreateResponseDto register(final UserCreateDto dto) {
        final String normalizedEmail = dto.email().toLowerCase().trim();
        final String encodedPassword = passwordEncoder.encode(dto.password());

        final Optional<AppUser> foundUserByEmail = userService.getByEmail(normalizedEmail);
        if (foundUserByEmail.isPresent()) {
            return handleExistingUser(foundUserByEmail.get());
        }

        final AppUser appUser;
        // Conditionally include displayName if the optional field is present and not blank
        if (dto.displayName() != null && !dto.displayName().isBlank()) {
            appUser = new AppUser(encodedPassword, normalizedEmail, dto.displayName());
        } else {
            appUser = new AppUser(encodedPassword, normalizedEmail);
        }

        final AppUser savedNewUser = userService.saveOrUpdate(appUser);

        String confirmationCode = confirmationService.generateConfirmationCode(savedNewUser);
        emailService.sendConfirmationEmail(savedNewUser.getEmail(), confirmationCode);

        return new UserCreateResponseDto(
                savedNewUser.getId().toString(),
                savedNewUser.getDisplayName(),
                savedNewUser.getEmail(),
                savedNewUser.getRole().name(),
                false
        );
    }


    private UserCreateResponseDto handleExistingUser(AppUser existingUser) {
        if (UNCONFIRMED.equals(existingUser.getConfirmationStatus())) {
            String confirmationCode = confirmationService.regenerateCode(existingUser);
            emailService.sendConfirmationEmail(existingUser.getEmail(), confirmationCode);
            return new UserCreateResponseDto(
                    existingUser.getId().toString(),
                    existingUser.getDisplayName(),
                    existingUser.getEmail(),
                    existingUser.getRole().name(),
                    true);
        }
        throw new UserAlreadyExistException();
    }

    @Transactional
    public UserResponseDto confirmRegistration(final String code) {
        final ConfirmationCode confirmationToken = confirmationService.getConfirmationIfValidOrThrow(code);

        final AppUser registeredUser = confirmationToken.getUser();
        registeredUser.setConfirmationStatus(CONFIRMED);
        userService.saveOrUpdate(registeredUser);

        confirmationService.removeToken(confirmationToken);

        return new UserResponseDto(
                registeredUser.getId().toString(),
                registeredUser.getDisplayName(),
                registeredUser.getEmail(),
                registeredUser.getPosition(),
                registeredUser.getDepartment(),
                registeredUser.getAvatarUrl(),
                registeredUser.getBio(),
                registeredUser.getRole().name(),
                registeredUser.getConfirmationStatus()
        );
    }
}
