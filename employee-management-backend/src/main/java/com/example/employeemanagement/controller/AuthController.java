package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.LoginRequest;
import com.example.employeemanagement.dto.LoginResponse;
import com.example.employeemanagement.dto.RegisterRequest;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.security.JwtService;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.entity.Role;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterRequest request
    ) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists.");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully.");

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String requestedType = request.getLoginType();
        if ("ADMIN".equalsIgnoreCase(requestedType)) {
            if (user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("You are not authorized to login as Admin.");
            }
        } else if ("EMPLOYEE".equalsIgnoreCase(requestedType)) {
            if (user.getRole() != Role.EMPLOYEE) {
                throw new IllegalArgumentException("Please login using Admin.");
            }
        } else {
            throw new IllegalArgumentException("Invalid login type. Must be ADMIN or EMPLOYEE.");
        }

        String token = jwtService.generateToken(userDetails);

        Long employeeId = null;
        String department = null;
        String role = "ADMIN";

        if (user.getRole() == Role.EMPLOYEE) {
            if (user.getEmployee() != null) {
                employeeId = user.getEmployee().getId();
                if (user.getEmployee().getDepartment() != null) {
                    department = user.getEmployee().getDepartment().name();
                }
                role = user.getEmployee().getDesignation();
            } else {
                role = "EMPLOYEE";
            }
        }

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .employeeId(employeeId)
                .name(user.getName())
                .email(user.getEmail())
                .userType(user.getRole().name())
                .department(department)
                .role(role)
                .build();

        return ResponseEntity.ok(response);

    }

}
