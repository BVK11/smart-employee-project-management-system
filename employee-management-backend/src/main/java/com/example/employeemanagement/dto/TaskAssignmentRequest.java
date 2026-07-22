package com.example.employeemanagement.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class TaskAssignmentRequest { @NotNull private Long employeeId; @NotNull private Long projectId; }
