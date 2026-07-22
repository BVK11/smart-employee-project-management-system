package com.example.employeemanagement.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Set;
@Data public class EmployeeAssignmentRequest { @NotNull private Set<Long> employeeIds; }
