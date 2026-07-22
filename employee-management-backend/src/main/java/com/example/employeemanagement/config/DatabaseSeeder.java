package com.example.employeemanagement.config;

import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Seed Admin Account
        User adminUser = seedAdmin();

        // Seed Employees
        List<Employee> seededEmployees = seedEmployees();

        // Seed Projects & Tasks if empty
        if (projectRepository.count() == 0 && taskRepository.count() == 0 && !seededEmployees.isEmpty()) {
            seedProjectsAndTasks(seededEmployees);
        }
    }

    private User seedAdmin() {
        String adminEmail = "admin@company.com";
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
        if (existingAdmin.isPresent()) {
            return existingAdmin.get();
        }

        User admin = User.builder()
                .name("System Administrator")
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .build();

        return userRepository.save(admin);
    }

    private List<Employee> seedEmployees() {
        String employeePassword = passwordEncoder.encode("Employee@123");
        List<EmployeeSeedData> seedDataList = getEmployeeSeedData();
        List<Employee> createdEmployees = new ArrayList<>();

        for (EmployeeSeedData data : seedDataList) {
            // Check if User already exists
            Optional<User> existingUser = userRepository.findByEmail(data.email);
            if (existingUser.isPresent()) {
                // If user exists, check if employee also exists
                employeeRepository.findByEmail(data.email).ifPresent(createdEmployees::add);
                continue;
            }

            // Create User
            User user = User.builder()
                    .name(data.fullName)
                    .email(data.email)
                    .password(employeePassword)
                    .role(Role.EMPLOYEE)
                    .build();
            user = userRepository.save(user);

            // Create Employee
            String[] nameParts = data.fullName.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            Employee employee = Employee.builder()
                    .employeeCode(data.code)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(data.email)
                    .phone(data.phone)
                    .department(data.department)
                    .designation(data.designation)
                    .salary(data.salary)
                    .joiningDate(data.joiningDate)
                    .status("ACTIVE")
                    .user(user)
                    .build();

            Employee savedEmployee = employeeRepository.save(employee);
            createdEmployees.add(savedEmployee);
        }

        return createdEmployees;
    }

    private void seedProjectsAndTasks(List<Employee> employees) {
        LocalDate today = LocalDate.now();

        // Find some employees for assignments
        Employee devops = employees.stream().filter(e -> e.getDepartment() == Department.DEVOPS).findFirst().orElse(employees.get(0));
        Employee backend = employees.stream().filter(e -> e.getDepartment() == Department.BACKEND).findFirst().orElse(employees.get(0));
        Employee frontend = employees.stream().filter(e -> e.getDepartment() == Department.FRONTEND).findFirst().orElse(employees.get(0));
        Employee qa = employees.stream().filter(e -> e.getDepartment() == Department.QA).findFirst().orElse(employees.get(0));

        // Create Project 1: Employee Portal
        Project proj1 = Project.builder()
                .projectName("Employee Portal")
                .description("Internal employee management system")
                .priority("HIGH")
                .status("IN_PROGRESS")
                .startDate(today.minusDays(30))
                .endDate(today.plusDays(90))
                .employees(new HashSet<>(Arrays.asList(backend, frontend, qa)))
                .build();
        proj1 = projectRepository.save(proj1);

        // Link projects back to employees
        backend.getProjects().add(proj1);
        frontend.getProjects().add(proj1);
        qa.getProjects().add(proj1);

        // Create Project 2: Mobile App
        Project proj2 = Project.builder()
                .projectName("Mobile App")
                .description("Cross-platform mobile application for tracking tasks")
                .priority("MEDIUM")
                .status("IN_PROGRESS")
                .startDate(today.minusDays(15))
                .endDate(today.plusDays(120))
                .employees(new HashSet<>(Arrays.asList(backend, frontend)))
                .build();
        proj2 = projectRepository.save(proj2);
        backend.getProjects().add(proj2);
        frontend.getProjects().add(proj2);

        // Create Project 3: Cloud Migration
        Project proj3 = Project.builder()
                .projectName("Cloud Migration")
                .description("Migrate all infrastructure databases and services to cloud")
                .priority("HIGH")
                .status("PLANNED")
                .startDate(today.plusDays(30))
                .endDate(today.plusDays(180))
                .employees(new HashSet<>(Collections.singletonList(devops)))
                .build();
        proj3 = projectRepository.save(proj3);
        devops.getProjects().add(proj3);

        // Save employees with projects updated
        employeeRepository.saveAll(Arrays.asList(backend, frontend, qa, devops));

        // Create Tasks
        Task task1 = Task.builder()
                .title("Database Schema Design")
                .description("Design the initial schema and migrations for the employee portal database.")
                .status("COMPLETED")
                .progress(100)
                .priority("HIGH")
                .remarks("Database schema approved by architect.")
                .deadline(today.minusDays(10))
                .assignedDate(today.minusDays(15))
                .dueDate(today.minusDays(10))
                .completedDate(today.minusDays(10))
                .estimatedHours(16)
                .employee(backend)
                .project(proj1)
                .build();
        taskRepository.save(task1);

        Task task2 = Task.builder()
                .title("UI Component Development")
                .description("Create modular UI components for dashboard, filters and list views.")
                .status("IN_PROGRESS")
                .progress(60)
                .priority("MEDIUM")
                .remarks("Developing the login screen and role verification dropdown.")
                .deadline(today.plusDays(15))
                .assignedDate(today.minusDays(5))
                .dueDate(today.plusDays(15))
                .estimatedHours(40)
                .employee(frontend)
                .project(proj1)
                .build();
        taskRepository.save(task2);

        Task task3 = Task.builder()
                .title("API Integration Testing")
                .description("Write automated integration tests for JWT authentication and employee search endpoints.")
                .status("PENDING")
                .progress(0)
                .priority("LOW")
                .deadline(today.plusDays(25))
                .assignedDate(today.minusDays(2))
                .dueDate(today.plusDays(25))
                .estimatedHours(20)
                .employee(qa)
                .project(proj1)
                .build();
        taskRepository.save(task3);

        Task task4 = Task.builder()
                .title("Cloud Infrastructure Setup")
                .description("Provision development and staging environments on cloud platform.")
                .status("PENDING")
                .progress(0)
                .priority("HIGH")
                .deadline(today.plusDays(40))
                .assignedDate(today)
                .dueDate(today.plusDays(40))
                .estimatedHours(30)
                .employee(devops)
                .project(proj3)
                .build();
        taskRepository.save(task4);
    }

    private List<EmployeeSeedData> getEmployeeSeedData() {
        return Arrays.asList(
                // HR
                new EmployeeSeedData("EMP-101", "Emma Watson", "hr1@company.com", "555-0101", Department.HR, "HR Specialist", 60000.0, LocalDate.of(2023, 1, 15)),
                new EmployeeSeedData("EMP-102", "Liam Neeson", "hr2@company.com", "555-0102", Department.HR, "HR Manager", 95000.0, LocalDate.of(2022, 5, 20)),
                new EmployeeSeedData("EMP-103", "Olivia Colman", "hr3@company.com", "555-0103", Department.HR, "HR Recruiter", 55000.0, LocalDate.of(2023, 8, 10)),

                // IT
                new EmployeeSeedData("EMP-104", "Noah Centineo", "it1@company.com", "555-0104", Department.IT, "IT Support Specialist", 50000.0, LocalDate.of(2023, 3, 12)),
                new EmployeeSeedData("EMP-105", "Ava DuVernay", "it2@company.com", "555-0105", Department.IT, "IT Systems Administrator", 75000.0, LocalDate.of(2022, 10, 1)),
                new EmployeeSeedData("EMP-106", "William Defoe", "it3@company.com", "555-0106", Department.IT, "IT Security Analyst", 85000.0, LocalDate.of(2023, 2, 28)),
                new EmployeeSeedData("EMP-107", "Sophia Loren", "it4@company.com", "555-0107", Department.IT, "IT Network Engineer", 80000.0, LocalDate.of(2021, 11, 15)),

                // Backend Developers
                new EmployeeSeedData("EMP-108", "Mason Mount", "backend1@company.com", "555-0108", Department.BACKEND, "Senior Backend Developer", 110000.0, LocalDate.of(2021, 6, 1)),
                new EmployeeSeedData("EMP-109", "Isabella Rossellini", "backend2@company.com", "555-0109", Department.BACKEND, "Java Developer", 85000.0, LocalDate.of(2023, 4, 18)),
                new EmployeeSeedData("EMP-110", "Jacob Elordi", "backend3@company.com", "555-0110", Department.BACKEND, "Backend Engineer", 75000.0, LocalDate.of(2023, 9, 5)),

                // Frontend Developers
                new EmployeeSeedData("EMP-111", "Lucas Hedges", "frontend1@company.com", "555-0111", Department.FRONTEND, "React Developer", 85000.0, LocalDate.of(2023, 2, 1)),
                new EmployeeSeedData("EMP-112", "Mia Farrow", "frontend2@company.com", "555-0112", Department.FRONTEND, "Frontend Engineer", 75000.0, LocalDate.of(2023, 7, 22)),
                new EmployeeSeedData("EMP-113", "Ethan Hawke", "frontend3@company.com", "555-0113", Department.FRONTEND, "UI Developer", 80000.0, LocalDate.of(2022, 9, 14)),

                // QA
                new EmployeeSeedData("EMP-114", "Charlotte Gainsbourg", "qa1@company.com", "555-0114", Department.QA, "QA Engineer", 70000.0, LocalDate.of(2023, 5, 10)),
                new EmployeeSeedData("EMP-115", "Amara Miller", "qa2@company.com", "555-0115", Department.QA, "Quality Analyst", 65000.0, LocalDate.of(2023, 10, 1)),
                new EmployeeSeedData("EMP-116", "Alexander Skarsgard", "qa3@company.com", "555-0116", Department.QA, "Senior QA Engineer", 95000.0, LocalDate.of(2022, 1, 15)),

                // API Testing
                new EmployeeSeedData("EMP-117", "Harper Lee", "api1@company.com", "555-0117", Department.API_TESTING, "API Testing Specialist", 72000.0, LocalDate.of(2023, 4, 1)),
                new EmployeeSeedData("EMP-118", "Daniel Kaluuya", "api2@company.com", "555-0118", Department.API_TESTING, "Backend QA Tester", 68000.0, LocalDate.of(2023, 8, 12)),

                // DevOps
                new EmployeeSeedData("EMP-119", "James McAvoy", "devops1@company.com", "555-0119", Department.DEVOPS, "DevOps Engineer", 100000.0, LocalDate.of(2022, 8, 1)),
                new EmployeeSeedData("EMP-120", "Emily Blunt", "devops2@company.com", "555-0120", Department.DEVOPS, "Cloud Infrastructure Engineer", 105000.0, LocalDate.of(2022, 3, 20)),

                // UI/UX
                new EmployeeSeedData("EMP-121", "Benjamin Bratt", "ui1@company.com", "555-0121", Department.UI_UX, "UI/UX Designer", 80000.0, LocalDate.of(2023, 6, 15)),
                new EmployeeSeedData("EMP-122", "Chloe Grace Moretz", "ui2@company.com", "555-0122", Department.UI_UX, "Product Designer", 85000.0, LocalDate.of(2023, 11, 1)),

                // Business Analyst
                new EmployeeSeedData("EMP-123", "Henry Cavill", "ba1@company.com", "555-0123", Department.BUSINESS_ANALYST, "Business Analyst", 85000.0, LocalDate.of(2023, 1, 10)),
                new EmployeeSeedData("EMP-124", "Amelia Earhart", "ba2@company.com", "555-0124", Department.BUSINESS_ANALYST, "Product Owner", 98000.0, LocalDate.of(2022, 7, 5)),

                // Support
                new EmployeeSeedData("EMP-125", "Sebastian Stan", "support1@company.com", "555-0125", Department.SUPPORT, "Support Specialist", 48000.0, LocalDate.of(2023, 5, 25)),
                new EmployeeSeedData("EMP-126", "Zoe Saldana", "support2@company.com", "555-0126", Department.SUPPORT, "Customer Support Agent", 46000.0, LocalDate.of(2023, 9, 30))
        );
    }

    private static class EmployeeSeedData {
        String code;
        String fullName;
        String email;
        String phone;
        Department department;
        String designation;
        double salary;
        LocalDate joiningDate;

        EmployeeSeedData(String code, String fullName, String email, String phone, Department department, String designation, double salary, LocalDate joiningDate) {
            this.code = code;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.department = department;
            this.designation = designation;
            this.salary = salary;
            this.joiningDate = joiningDate;
        }
    }
}
