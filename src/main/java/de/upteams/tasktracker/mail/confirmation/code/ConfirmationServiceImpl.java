package de.upteams.tasktracker.mail.confirmation.code;

import de.upteams.tasktracker.mail.confirmation.code.interfaces.ConfirmationCodeRepository;
import de.upteams.tasktracker.mail.confirmation.code.interfaces.ConfirmationService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.exception.UserConfirmationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for various operations with Confirmation Codes
 */
@Service
@RequiredArgsConstructor
public class ConfirmationServiceImpl implements ConfirmationService {


    @Value("${confirmation.expiration.days:5}")
    private int confirmationDays;
    private final ConfirmationCodeRepository repository;

    @Override
    public String generateConfirmationCode(final AppUser user) {
        return generateConfirmation(user).getId().toString();
    }

    @Override
    public ConfirmationCode generateConfirmation(final AppUser user) {
        final LocalDateTime expiration = getExpiration(confirmationDays);
        return repository.save(new ConfirmationCode(expiration, user));
    }

    @Override
    public String regenerateCode(final AppUser existingUser) {
        final Optional<ConfirmationCode> optionalCode = getConfirmationCode(existingUser);
        if (optionalCode.isPresent()) {
            final ConfirmationCode existingCode = optionalCode.get();
            final LocalDateTime newExpiration = getExpiration(confirmationDays);

            existingCode.setExpired(newExpiration);
            repository.save(existingCode);

            return existingCode.getId().toString();
        } else {
            return generateConfirmationCode(existingUser);
        }
    }

    @Override
    public ConfirmationCode getConfirmationIfValidOrThrow(final String code) {
        ConfirmationCode codeEntity = getConfirmationCode(code)
                .orElseThrow(() -> new UserConfirmationException("Confirmation Code not found"));

        if (codeEntity.getExpired().isBefore(LocalDateTime.now())) {
            throw new UserConfirmationException("Confirmation Code is already expired");
        }

        return codeEntity;
    }

    @Override
    public void removeToken(ConfirmationCode code) {
        repository.delete(code);
    }

    private Optional<ConfirmationCode> getConfirmationCode(AppUser user) {
        return repository.findByUser(user);
    }

    private Optional<ConfirmationCode> getConfirmationCode(String code) {
        return repository.findByCode(UUID.fromString(code));
    }

    private static LocalDateTime getExpiration(int confirmationExpirationDays) {
        return LocalDateTime.of(LocalDate.now().plusDays(confirmationExpirationDays), LocalTime.MAX);
    }
}
