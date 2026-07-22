package com.example.employeemanagement.exception;

import com.example.employeemanagement.dto.ErrorDetailsDTO;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetailsDTO> validation(MethodArgumentNotValidException ex) { Map<String, String> fields = new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage())); return response(HttpStatus.BAD_REQUEST, "Validation failed", fields); }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetailsDTO> notFound(ResourceNotFoundException ex) { return response(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of()); }
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorDetailsDTO> duplicateEmail(DuplicateEmailException ex) { return response(HttpStatus.CONFLICT, ex.getMessage(), Map.of()); }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetailsDTO> badRequest(IllegalArgumentException ex) { return response(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of()); }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetailsDTO> unauthorized(AuthenticationException ex) { return response(HttpStatus.UNAUTHORIZED, "Authentication is required", Map.of()); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetailsDTO> forbidden(AccessDeniedException ex) { return response(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", Map.of()); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetailsDTO> general(Exception ex) { return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", Map.of()); }
    private ResponseEntity<ErrorDetailsDTO> response(HttpStatus status, String message, Map<String, String> details) { return ResponseEntity.status(status).body(ErrorDetailsDTO.builder().timestamp(LocalDateTime.now()).message(message).details(details).httpStatus(status.value()).build()); }
}
