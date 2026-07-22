package com.example.employeemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskDTO {
    private Long id;
    @NotBlank(message = "Title is required") private String title;
    private String description;
    @NotBlank @Pattern(regexp = "PENDING|IN_PROGRESS|COMPLETED", message = "Status must be PENDING, IN_PROGRESS or COMPLETED") private String status;
    @NotNull @Min(0) @Max(100) private Integer progress;
    private String priority;
    private String remarks;
    private LocalDate deadline;
    private LocalDate assignedDate;
    private LocalDate dueDate;
    private LocalDate completedDate;
    private Integer estimatedHours;
    private Long employeeId;
    private String employeeName;
    @NotNull(message = "Project id is required") private Long projectId;
    private String projectName;
}
