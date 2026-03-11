package de.upteams.tasktracker.user.entity;

import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ColumnDefault;

/**
 * Application User entity representing a registered user in the system
 * The user account lifecycle includes:
 * Registration with email and password
 * Email confirmation (UNCONFIRMED → CONFIRMED)
 * Profile completion (display name, position, etc.)
 * Optional account ban (BANNED status)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    /**
     * User's encrypted password.
     * Never stored in plain text - always encoded using BCrypt.
     * This field is mandatory for authentication.
     */
    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "{user.email.notBlank}")
    @Column(
            name = "email",
            unique = true,
            nullable = false,
            columnDefinition = "VARCHAR(255) COLLATE ascii_bin"
    )
    private String email;

    /**
     * User's display name shown throughout the application.
     * Can be automatically generated from email if not provided during registration.
     * This field is optional but recommended for better user experience.
     */
    @Column(name = "display_name")
    private String displayName;

    @Column(name = "position")
    private String position;

    @Column(name = "department")
    private String department;

    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * Short biography or "about me" text.
     * Limited to 1000 characters to maintain reasonable database size.
     * Can include user's skills, experience, or personal description.
     */
    @Column(name = "bio", length = 1000)
    private String bio;

    /**
     * Current confirmation status of the user account.
     * - UNCONFIRMED: Newly registered, email not verified
     * - CONFIRMED: Email verified, account fully functional
     * - BANNED: Account suspended by administrator
     * Defaults to UNCONFIRMED for new registrations.
     */
    @NotNull(message = "{field.notNull}")
    @Column(name = "confirm_status", nullable = false)
    @ColumnDefault("'UNCONFIRMED'")
    @Enumerated(EnumType.STRING)
    private ConfirmationStatus confirmationStatus = ConfirmationStatus.UNCONFIRMED;

    @NotNull(message = "{field.notNull}")
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Constructs a new user with minimal required information.
     * Automatically generates a display name from the email address.
     *
     * @param password the encrypted password
     * @param email    the user's email address
     */
    public AppUser(String password, String email) {
        this.password = password;
        this.email = email;
        this.displayName = extractDisplayNameFromEmail(email);
        role = Role.ROLE_USER;
    }

    /**
     * Constructs a new user with password, email, and custom display name.
     *
     * @param password    the encrypted password
     * @param email       the user's email address
     * @param displayName the user's preferred display name
     */
    public AppUser(String password, String email, String displayName) {
        this.password = password;
        this.email = email;
        this.displayName = displayName;
        this.role = Role.ROLE_USER;
    }

    /**
     * Extracts a display name from email address
     * Example: "homer.simpson@company.com" -> "Homer Simpson"
     * "homer_impson@company.com" → "Homer Simpson"
     * "homer@company.com" → "Homer"
     */
    private String extractDisplayNameFromEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        // Take part before @
        String localPart = email.substring(0, email.indexOf('@'));

        // Replace dots and underscores with spaces
        String name = localPart.replaceAll("[._]", " ");

        // Capitalize first letters
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : name.toCharArray()) {
            if (capitalizeNext && Character.isLetter(c)) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
            if (c == ' ') {
                capitalizeNext = true;
            }
        }

        return result.toString().trim();
    }

    /**
     * Updates user profile information
     * Only non-blank fields will be updated; null or empty values are ignored.
     *  @param displayName new display name (ignored if null/blank)
     *  @param position    new job position (ignored if null/blank)
     *  @param department  new department (ignored if null/blank)
     *  @param bio         new biography (ignored if null/blank)
     */
    public void updateProfile(String displayName, String position,
                              String department, String bio) {
        if (displayName != null && !displayName.isBlank()) {
            this.displayName = displayName;
        }
        if (position != null && !position.isBlank()) {
            this.position = position;
        }
        if (department != null && !department.isBlank()) {
            this.department = department;
        }
        if (bio != null && !bio.isBlank()) {
            this.bio = bio;
        }
    }

    /**
     * Updates or sets the user's avatar URL.
     * @param avatarUrl the new avatar URL (can be null to remove avatar)
     */
    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String toString() {
        return String.format("AppUser{id=%s, email='%s', displayName='%s', position='%s', department='%s', confirmationStatus=%s, role=%s, hasAvatar=%b}",
                id,         // %s for UUID
                email,
                displayName,
                position,
                department,
                confirmationStatus,
                role,
                avatarUrl != null && !avatarUrl.isBlank());
    }
}
