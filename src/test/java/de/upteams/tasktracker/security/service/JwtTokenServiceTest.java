package de.upteams.tasktracker.security.service;


import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenServiceTest {

    @Test
    void shouldCreateKeysWithValidSecrets() {
        String base64Secret_1 = "eW91ci0yNTYtYml0LXNlY3JldC1mb3ItYWNjZXNzLXRva2VuLTAxMjM0NTY3ODlhYmNkZWY==";
        String base64Secret_2 = "eW91ci0yNTYtYml0LXNlY3JldC1mb3ItcmVmcmVzaC10b2tlbi1hYmNkZWYwMTIzNDU2Nzg5";
        assertDoesNotThrow(() -> new JwtTokenService(base64Secret_1, base64Secret_2));
    }

    @Test
    void shouldThrowOnShortSecret() {
        String shortSecret = Base64.getEncoder().encodeToString("short".getBytes());
        assertThrows(IllegalArgumentException.class,
                () -> new JwtTokenService(shortSecret, shortSecret));
    }
}
