package com.example.employeemanagement.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeReportDTO { private String employeeName; private long assignedTasks; private long completedTasks; private long pendingTasks; }
