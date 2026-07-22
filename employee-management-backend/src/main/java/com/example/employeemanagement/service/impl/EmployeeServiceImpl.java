package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.EmployeeDTO;
import com.example.employeemanagement.dto.PageResponse;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.EmployeeService;
import com.example.employeemanagement.specification.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        employeeRepository.findByEmail(dto.getEmail()).ifPresent(e -> { throw new DuplicateEmailException("Employee email already exists"); });
        Employee employee = map(dto); employee.setEmployeeCode(dto.getEmployeeCode() == null || dto.getEmployeeCode().isBlank() ? "EMP-" + System.currentTimeMillis() : dto.getEmployeeCode());
        return map(employeeRepository.save(employee));
    }
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = find(id); employeeRepository.findByEmail(dto.getEmail()).filter(e -> !e.getId().equals(id)).ifPresent(e -> { throw new DuplicateEmailException("Employee email already exists"); });
        employee.setFirstName(dto.getFirstName()); employee.setLastName(dto.getLastName()); employee.setEmail(dto.getEmail()); employee.setPhone(dto.getPhone()); employee.setDepartment(dto.getDepartment()); employee.setDesignation(dto.getDesignation()); employee.setSalary(dto.getSalary()); employee.setJoiningDate(dto.getJoiningDate()); employee.setStatus(dto.getStatus());
        return map(employeeRepository.save(employee));
    }
    public void deleteEmployee(Long id) { employeeRepository.delete(find(id)); }
    public EmployeeDTO getEmployeeById(Long id) { return map(find(id)); }
    public PageResponse<EmployeeDTO> getAllEmployees(int page, int size, String sortBy, String sortDir) { return page(employeeRepository.findAll(pageable(page, size, sortBy, sortDir))); }
    public PageResponse<EmployeeDTO> searchEmployees(String department, String status, String keyword, int page, int size, String sortBy, String sortDir) {
        Specification<Employee> specification = Specification.where(EmployeeSpecification.hasDepartment(department)).and(EmployeeSpecification.hasStatus(status)).and(EmployeeSpecification.hasKeyword(keyword));
        return page(employeeRepository.findAll(specification, pageable(page, size, sortBy, sortDir)));
    }
    private Employee find(Long id) { return employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id)); }
    private Pageable pageable(int page, int size, String sortBy, String sortDir) { if (page < 0 || size < 1) throw new IllegalArgumentException("Page must be >= 0 and size must be > 0"); return PageRequest.of(page, size, "desc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()); }
    private PageResponse<EmployeeDTO> page(Page<Employee> result) { return PageResponse.<EmployeeDTO>builder().content(result.map(this::map).getContent()).pageNo(result.getNumber()).pageSize(result.getSize()).totalElements(result.getTotalElements()).totalPages(result.getTotalPages()).last(result.isLast()).build(); }
    private Employee map(EmployeeDTO dto) { return Employee.builder().id(dto.getId()).employeeCode(dto.getEmployeeCode()).firstName(dto.getFirstName()).lastName(dto.getLastName()).email(dto.getEmail()).phone(dto.getPhone()).department(dto.getDepartment()).designation(dto.getDesignation()).salary(dto.getSalary()).joiningDate(dto.getJoiningDate()).status(dto.getStatus()).build(); }
    private EmployeeDTO map(Employee employee) { return EmployeeDTO.builder().id(employee.getId()).employeeCode(employee.getEmployeeCode()).firstName(employee.getFirstName()).lastName(employee.getLastName()).email(employee.getEmail()).phone(employee.getPhone()).department(employee.getDepartment()).designation(employee.getDesignation()).salary(employee.getSalary()).joiningDate(employee.getJoiningDate()).status(employee.getStatus()).build(); }
}
