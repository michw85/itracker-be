package de.upteams.tasktracker.security.controller;

import de.upteams.tasktracker.security.dto.LoginRequest;
import de.upteams.tasktracker.security.entities.RefreshRequestDto;
import de.upteams.tasktracker.security.entities.TokenResponseDto;
import de.upteams.tasktracker.security.service.AuthService;
import de.upteams.tasktracker.security.service.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import static de.upteams.tasktracker.security.constants.Constants.ACCESS_TOKEN_COOKIE;
import static de.upteams.tasktracker.security.constants.Constants.REFRESH_TOKEN_COOKIE;

/**
 * Controller that receives authorization http requests
 */
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService service;
    private final CookieService cookieService;

    @Override
    public TokenResponseDto login(LoginRequest loginRequest, HttpServletResponse response) {
        final TokenResponseDto tokens = service.login(loginRequest);

        final Cookie accessCookie = cookieService.generateAccessTokenCookie(tokens.getAccessToken());
        final Cookie refreshCookie = cookieService.generateRefreshTokenCookie(tokens.getRefreshToken());

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return tokens;
    }

    @Override
    public TokenResponseDto refreshAccessToken(RefreshRequestDto request, HttpServletResponse response) {
        final String newAccessToken = service.refreshAccessToken(request.getRefreshToken());
        final Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, newAccessToken);

        response.addCookie(accessCookie);
        return new TokenResponseDto(newAccessToken, request.getRefreshToken());
    }

    @Override
    public TokenResponseDto logout(HttpServletResponse response) {
        final Cookie accessCookie = cookieService.generateLogoutCookie(ACCESS_TOKEN_COOKIE);
        final Cookie refreshCookie = cookieService.generateLogoutCookie(REFRESH_TOKEN_COOKIE);
        SecurityContextHolder.clearContext();

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return new TokenResponseDto(null, null);
    }
}
