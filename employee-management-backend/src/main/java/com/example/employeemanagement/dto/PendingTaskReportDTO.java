package com.example.employeemanagement.dto;
import lombok.*;
import java.time.LocalDate;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PendingTaskReportDTO { private String employeeName; private String projectName; private LocalDate deadline; private String priority; private String status; }
