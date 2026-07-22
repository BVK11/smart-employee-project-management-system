package com.example.employeemanagement.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class RemarksUpdateRequest { @NotBlank private String remarks; }
