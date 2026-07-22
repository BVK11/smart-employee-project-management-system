package com.example.employeemanagement.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectProgressReportDTO { private String projectName; private long assignedEmployeesCount; private long totalTasks; private long completedTasks; private long remainingTasks; private double progressPercentage; }
