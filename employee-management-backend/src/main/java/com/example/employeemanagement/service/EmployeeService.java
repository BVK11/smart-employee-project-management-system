package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeDTO;
import com.example.employeemanagement.dto.PageResponse;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
    void deleteEmployee(Long id);
    EmployeeDTO getEmployeeById(Long id);
    PageResponse<EmployeeDTO> getAllEmployees(int page, int size, String sortBy, String sortDir);
    PageResponse<EmployeeDTO> searchEmployees(String department, String status, String keyword, int page, int size, String sortBy, String sortDir);
}
