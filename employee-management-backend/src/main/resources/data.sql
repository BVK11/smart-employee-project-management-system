-- Seed data for Employee Management System
-- Users (passwords are encoded with BCrypt)
-- Admin user: admin@example.com / Password123
-- Employee users: emp1@example.com / Employee123, emp2@example.com / Employee123

-- Insert Admin User
INSERT IGNORE INTO users (id, email, name, password, role) VALUES 
(1, 'admin@example.com', 'Admin User', '$2a$10$N9qo8uLOickgx2ZMRZoMyu5Z5KyYXmVe5aw7zHgFvKqxXz2R9D.Ly', 'ADMIN');

-- Insert Employee Users
INSERT IGNORE INTO users (id, email, name, password, role) VALUES 
(2, 'emp1@example.com', 'John Doe', '$2a$10$N9qo8uLOickgx2ZMRZoMyu5Z5KyYXmVe5aw7zHgFvKqxXz2R9D.Ly', 'EMPLOYEE'),
(3, 'emp2@example.com', 'Jane Smith', '$2a$10$N9qo8uLOickgx2ZMRZoMyu5Z5KyYXmVe5aw7zHgFvKqxXz2R9D.Ly', 'EMPLOYEE'),
(4, 'emp3@example.com', 'Bob Johnson', '$2a$10$N9qo8uLOickgx2ZMRZoMyu5Z5KyYXmVe5aw7zHgFvKqxXz2R9D.Ly', 'EMPLOYEE');

-- Insert Employee Details
INSERT IGNORE INTO employee (id, user_id, department, position, salary, hire_date, phone, address) VALUES 
(1, 2, 'Engineering', 'Software Engineer', 80000.00, '2023-01-15', '555-0001', '123 Main St'),
(2, 3, 'Marketing', 'Marketing Manager', 75000.00, '2023-02-20', '555-0002', '456 Oak Ave'),
(3, 4, 'Sales', 'Sales Executive', 70000.00, '2023-03-10', '555-0003', '789 Pine Rd');

-- Insert Projects
INSERT IGNORE INTO project (id, name, description, start_date, end_date, status) VALUES 
(1, 'Employee Portal', 'Internal employee management system', '2024-01-01', '2024-06-30', 'IN_PROGRESS'),
(2, 'Mobile App', 'Cross-platform mobile application', '2024-02-15', '2024-09-30', 'IN_PROGRESS'),
(3, 'Cloud Migration', 'Migrate infrastructure to cloud', '2024-03-01', '2024-08-31', 'PLANNED');

-- Insert Tasks
INSERT IGNORE INTO task (id, project_id, assigned_to, title, description, status, priority, start_date, end_date) VALUES 
(1, 1, 2, 'Database Schema Design', 'Design the database schema for employee portal', 'COMPLETED', 'HIGH', '2024-01-01', '2024-01-15'),
(2, 1, 3, 'UI Development', 'Create user interface components', 'IN_PROGRESS', 'HIGH', '2024-01-16', '2024-02-15'),
(3, 2, 4, 'API Development', 'Build REST APIs for mobile app', 'IN_PROGRESS', 'MEDIUM', '2024-02-15', '2024-04-15'),
(4, 3, 2, 'Infrastructure Setup', 'Setup cloud infrastructure', 'PENDING', 'HIGH', '2024-03-01', '2024-04-01');

