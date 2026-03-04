package de.upteams.tasktracker.security.service;

import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter for wrapping User entity into Spring Security UserDetails.
 */
public record AuthUserDetails(AppUser user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonLocked() {
        var confirmationStatus = user.getConfirmationStatus();
        return !ConfirmationStatus.BANNED.equals(confirmationStatus);
    }

    @Override
    public boolean isEnabled() {
        var confirmationStatus = user.getConfirmationStatus();
        return ConfirmationStatus.CONFIRMED.equals(confirmationStatus);
    }
}
