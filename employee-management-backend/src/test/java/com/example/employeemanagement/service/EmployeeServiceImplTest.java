package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeDTO;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeServiceImpl.
 * All repository dependencies are mocked — no database connection needed.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDTO sampleDto;
    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleDto = EmployeeDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@company.com")
                .phone("555-1234")
                .department(Department.BACKEND)
                .designation("Senior Developer")
                .salary(80000.0)
                .joiningDate(LocalDate.of(2022, 1, 15))
                .status("ACTIVE")
                .build();

        sampleEmployee = Employee.builder()
                .id(1L)
                .employeeCode("EMP-001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@company.com")
                .phone("555-1234")
                .department(Department.BACKEND)
                .designation("Senior Developer")
                .salary(80000.0)
                .joiningDate(LocalDate.of(2022, 1, 15))
                .status("ACTIVE")
                .build();
    }

    // ─────────────────────────── createEmployee ──────────────────────────────

    @Test
    @DisplayName("createEmployee() saves and returns the employee DTO when email is unique")
    void createEmployee_succeeds_whenEmailIsUnique() {
        when(employeeRepository.findByEmail(sampleDto.getEmail())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        EmployeeDTO result = employeeService.createEmployee(sampleDto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john.doe@company.com");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee() throws DuplicateEmailException when email already exists")
    void createEmployee_throwsDuplicateEmailException_whenEmailExists() {
        when(employeeRepository.findByEmail(sampleDto.getEmail()))
                .thenReturn(Optional.of(sampleEmployee));

        assertThatThrownBy(() -> employeeService.createEmployee(sampleDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("Employee email already exists");

        verify(employeeRepository, never()).save(any());
    }

    // ─────────────────────────── updateEmployee ──────────────────────────────

    @Test
    @DisplayName("updateEmployee() updates and returns the employee DTO when found")
    void updateEmployee_succeeds_whenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeRepository.findByEmail(sampleDto.getEmail())).thenReturn(Optional.empty());

        Employee updated = Employee.builder()
                .id(1L)
                .employeeCode("EMP-001")
                .firstName("John")
                .lastName("Smith")
                .email("john.doe@company.com")
                .phone("555-9999")
                .department(Department.FRONTEND)
                .designation("Lead Developer")
                .salary(95000.0)
                .joiningDate(LocalDate.of(2022, 1, 15))
                .status("ACTIVE")
                .build();

        EmployeeDTO updateDto = EmployeeDTO.builder()
                .firstName("John").lastName("Smith").email("john.doe@company.com")
                .phone("555-9999").department(Department.FRONTEND)
                .designation("Lead Developer").salary(95000.0)
                .joiningDate(LocalDate.of(2022, 1, 15)).status("ACTIVE")
                .build();

        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);

        EmployeeDTO result = employeeService.updateEmployee(1L, updateDto);

        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getDepartment()).isEqualTo(Department.FRONTEND);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployee() throws ResourceNotFoundException when employee does not exist")
    void updateEmployee_throwsResourceNotFoundException_whenNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(999L, sampleDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 999");
    }

    // ─────────────────────────── deleteEmployee ──────────────────────────────

    @Test
    @DisplayName("deleteEmployee() deletes the employee when found")
    void deleteEmployee_succeeds_whenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(sampleEmployee);
    }

    @Test
    @DisplayName("deleteEmployee() throws ResourceNotFoundException when employee does not exist")
    void deleteEmployee_throwsResourceNotFoundException_whenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");

        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    // ──────────────────────────── getEmployeeById ────────────────────────────

    @Test
    @DisplayName("getEmployeeById() returns the employee DTO when found")
    void getEmployeeById_returnsDto_whenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john.doe@company.com");
    }

    @Test
    @DisplayName("getEmployeeById() throws ResourceNotFoundException when employee does not exist")
    void getEmployeeById_throwsResourceNotFoundException_whenNotFound() {
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 42");
    }

    // ─────────────────────────── getAllEmployees ──────────────────────────────

    @Test
    @DisplayName("getAllEmployees() returns a page response containing all employees")
    void getAllEmployees_returnsPageResponse() {
        Page<Employee> page = new PageImpl<>(List.of(sampleEmployee), PageRequest.of(0, 10), 1);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);

        var result = employeeService.getAllEmployees(0, 10, "id", "asc");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("john.doe@company.com");
    }

    @Test
    @DisplayName("getAllEmployees() throws IllegalArgumentException for invalid pagination parameters")
    void getAllEmployees_throwsException_forInvalidPagination() {
        assertThatThrownBy(() -> employeeService.getAllEmployees(-1, 10, "id", "asc"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
