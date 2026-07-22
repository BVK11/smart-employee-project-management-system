package com.example.employeemanagement.specification;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.Department;
import org.springframework.data.jpa.domain.Specification;
public final class EmployeeSpecification {
    private EmployeeSpecification() {}
    public static Specification<Employee> hasDepartment(String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) return null;
            try {
                Department dept = Department.valueOf(value.toUpperCase().trim());
                return cb.equal(root.get("department"), dept);
            } catch (IllegalArgumentException e) {
                return cb.like(cb.lower(root.get("department").as(String.class)), "%" + value.toLowerCase() + "%");
            }
        };
    }
    public static Specification<Employee> hasStatus(String value) { return (root, query, cb) -> value == null || value.isBlank() ? null : cb.equal(cb.lower(root.get("status")), value.toLowerCase()); }
    public static Specification<Employee> hasKeyword(String value) { return (root, query, cb) -> { if (value == null || value.isBlank()) return null; String search = "%" + value.toLowerCase() + "%"; return cb.or(cb.like(cb.lower(root.get("firstName")), search), cb.like(cb.lower(root.get("lastName")), search), cb.like(cb.lower(root.get("email")), search), cb.like(cb.lower(root.get("designation")), search)); }; }
}
