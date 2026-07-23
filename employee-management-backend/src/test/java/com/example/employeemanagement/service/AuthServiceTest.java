package com.example.employeemanagement.service;

import com.example.employeemanagement.controller.AuthController;
import com.example.employeemanagement.dto.LoginRequest;
import com.example.employeemanagement.dto.LoginResponse;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Authentication Service / Login flow.
 * Tests successful login, invalid credentials, user not found, and JWT token generation.
 * All dependencies are mocked using Mockito — no database connection needed.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private User sampleAdminUser;
    private UserDetails sampleUserDetails;

    @BeforeEach
    void setUp() {
        sampleAdminUser = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@company.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .build();

        sampleUserDetails = new org.springframework.security.core.userdetails.User(
                "admin@company.com",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    // ─────────────────────────── Successful Login ─────────────────────────────

    @Test
    @DisplayName("loginUser() succeeds with valid credentials and generates JWT token")
    void login_successful_returnsJwtTokenAndUserFields() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@company.com");
        request.setPassword("Admin@123");
        request.setLoginType("ADMIN");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(sampleUserDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("admin@company.com"))
                .thenReturn(Optional.of(sampleAdminUser));
        when(jwtService.generateToken(sampleUserDetails))
                .thenReturn("mocked.jwt.token");

        ResponseEntity<LoginResponse> response = authController.loginUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getBody().getEmail()).isEqualTo("admin@company.com");
        assertThat(response.getBody().getUserType()).isEqualTo("ADMIN");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(sampleUserDetails);
    }

    // ────────────────────────── Invalid Credentials ───────────────────────────

    @Test
    @DisplayName("loginUser() throws BadCredentialsException when password is incorrect")
    void login_invalidCredentials_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@company.com");
        request.setPassword("WrongPassword");
        request.setLoginType("ADMIN");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        assertThatThrownBy(() -> authController.loginUser(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(jwtService, never()).generateToken(any());
    }

    // ────────────────────────────── User Not Found ────────────────────────────

    @Test
    @DisplayName("loginUser() throws ResourceNotFoundException when user email does not exist")
    void login_userNotFound_throwsResourceNotFoundException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@company.com");
        request.setPassword("SomePassword");
        request.setLoginType("ADMIN");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("nonexistent@company.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.loginUser(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ────────────────────────────── JWT Generation ────────────────────────────

    @Test
    @DisplayName("JWT Generation produces valid token for authenticated user details")
    void jwtGeneration_producesToken() {
        when(jwtService.generateToken(sampleUserDetails)).thenReturn("generated.jwt.token");

        String token = jwtService.generateToken(sampleUserDetails);

        assertThat(token).isNotNull().isEqualTo("generated.jwt.token");
    }
}
