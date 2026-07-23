package com.example.employeemanagement.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService.
 * Tests token generation, username extraction, and validation logic.
 * No database or Spring context needed — uses ReflectionTestUtils to inject config values.
 */
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET =
            "MySuperSecretKeyForEmployeeManagementProject2026JwtSecretKey123456789";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
        jwtService.init();
    }

    private UserDetails buildUserDetails(String email, String role) {
        return new User(
                email,
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    // ──────────────────────────── Token Generation ────────────────────────────

    @Test
    @DisplayName("generateToken() returns a non-null, non-blank JWT string")
    void generateToken_returnsNonBlankToken() {
        UserDetails user = buildUserDetails("admin@company.com", "ADMIN");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generateToken() produces a token containing three JWT segments separated by dots")
    void generateToken_hasThreeJwtSegments() {
        UserDetails user = buildUserDetails("employee@company.com", "EMPLOYEE");

        String token = jwtService.generateToken(user);
        String[] parts = token.split("\\.");

        assertThat(parts).hasSize(3);
    }

    // ──────────────────────────── Username Extraction ─────────────────────────

    @Test
    @DisplayName("extractUsername() returns the email used when generating the token")
    void extractUsername_returnsCorrectEmail() {
        String email = "backend1@company.com";
        UserDetails user = buildUserDetails(email, "EMPLOYEE");
        String token = jwtService.generateToken(user);

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(email);
    }

    // ──────────────────────────── Token Validation ────────────────────────────

    @Test
    @DisplayName("isTokenValid() returns true for a freshly-generated token")
    void isTokenValid_returnsTrueForFreshToken() {
        UserDetails user = buildUserDetails("admin@company.com", "ADMIN");
        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token, user);

        assertTrue(valid);
    }

    @Test
    @DisplayName("isTokenValid() returns false when the username does not match")
    void isTokenValid_returnsFalseForDifferentUser() {
        UserDetails originalUser = buildUserDetails("admin@company.com", "ADMIN");
        UserDetails otherUser   = buildUserDetails("other@company.com", "EMPLOYEE");
        String token = jwtService.generateToken(originalUser);

        boolean valid = jwtService.isTokenValid(token, otherUser);

        assertFalse(valid);
    }

    @Test
    @DisplayName("Parsing an expired token throws ExpiredJwtException")
    void isTokenValid_throwsExpiredJwtException_forExpiredToken() throws Exception {
        JwtService expiredService = new JwtService();
        ReflectionTestUtils.setField(expiredService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredService, "jwtExpiration", 1L); // 1 ms
        expiredService.init();

        UserDetails user = buildUserDetails("admin@company.com", "ADMIN");
        String token = expiredService.generateToken(user);

        Thread.sleep(10);

        assertThatThrownBy(() -> expiredService.isTokenValid(token, user))
                .isInstanceOf(ExpiredJwtException.class);
    }

    // ──────────────────────────── Expiration Extraction ──────────────────────

    @Test
    @DisplayName("extractExpiration() returns a date in the future for a fresh token")
    void extractExpiration_returnsFutureDate() {
        UserDetails user = buildUserDetails("admin@company.com", "ADMIN");
        String token = jwtService.generateToken(user);

        java.util.Date expiration = jwtService.extractExpiration(token);

        assertThat(expiration).isAfter(new java.util.Date());
    }
}
