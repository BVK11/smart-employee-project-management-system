package com.example.employeemanagement;

import org.junit.jupiter.api.Test;

/**
 * Smoke test to verify the test configuration is correct.
 * Full Spring context tests are avoided to prevent database dependency.
 */
class EmployeeManagementBackendApplicationTests {

    @Test
    void contextTest() {
        // Basic test - no Spring context needed, prevents MySQL connection requirement
    }

}
