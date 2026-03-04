package de.upteams.tasktracker.mail.confirmation.code.interfaces;

import de.upteams.tasktracker.mail.confirmation.code.ConfirmationCode;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.exception.UserConfirmationException;

/**
 * Service for various operations with Confirmation Codes
 */
public interface ConfirmationService {

    String generateConfirmationCode(AppUser user);

    ConfirmationCode generateConfirmation(AppUser user);

    String regenerateCode(AppUser existingUser);

    ConfirmationCode getConfirmationIfValidOrThrow(String code) throws UserConfirmationException;

    void removeToken(ConfirmationCode code);
}
