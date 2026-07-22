package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.Department;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String employeeCode;
    @NotBlank(message = "First name is required") private String firstName;
    @NotBlank(message = "Last name is required") private String lastName;
    @Email(message = "Invalid email format") @NotBlank(message = "Email is required") private String email;
    @NotBlank(message = "Phone number is required") private String phone;
    @NotNull(message = "Department is required") private Department department;
    @NotBlank(message = "Designation is required") private String designation;
    @NotNull(message = "Salary is required") @Positive(message = "Salary must be greater than zero") private Double salary;
    @NotNull(message = "Joining date is required") private LocalDate joiningDate;
    @NotBlank(message = "Status is required") private String status;
}
