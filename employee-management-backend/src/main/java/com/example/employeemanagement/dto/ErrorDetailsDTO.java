package com.example.employeemanagement.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorDetailsDTO { private LocalDateTime timestamp; private String message; private Map<String, String> details; private int httpStatus; }
