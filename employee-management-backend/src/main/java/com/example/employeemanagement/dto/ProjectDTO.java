package com.example.employeemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectDTO {
    private Long id;
    @NotBlank(message = "Project name is required") private String projectName;
    private String description;
    @NotBlank @Pattern(regexp = "HIGH|MEDIUM|LOW", message = "Priority must be HIGH, MEDIUM or LOW") private String priority;
    @NotBlank @Pattern(regexp = "ACTIVE|COMPLETED|ON_HOLD", message = "Status must be ACTIVE, COMPLETED or ON_HOLD") private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<Long> employeeIds;
    private Double progress;
}
