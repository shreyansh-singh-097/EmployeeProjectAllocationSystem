package com.college.service;

import com.college.dao.EmployeeDAO;
import com.college.model.Employee;
import com.college.util.PasswordUtil;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class EmployeeService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final EmployeeDAO employeeDAO;

    public EmployeeService() {
        this.employeeDAO = new EmployeeDAO();
    }

    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public void createEmployee(String name, String email, String designation, Double salary, String rawPassword, String role) {
        validateCommonFields(name, email, designation, salary);

        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required for a new employee.");
        }
        if (rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        if (employeeDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An employee with email '" + email + "' already exists.");
        }

        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        String finalRole = (role != null && role.equalsIgnoreCase("ADMIN")) ? "ADMIN" : "EMPLOYEE";

        Employee employee = new Employee(name.trim(), email.trim().toLowerCase(), designation.trim(), salary, hashedPassword, finalRole);
        employeeDAO.save(employee);
    }

    public void updateEmployee(int employeeId, String name, String email, String designation, Double salary, String rawPassword, String role) {
        validateCommonFields(name, email, designation, salary);

        Employee existing = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        if (!existing.getEmail().equalsIgnoreCase(email.trim())) {
            Optional<Employee> duplicate = employeeDAO.findByEmail(email);
            if (duplicate.isPresent() && !duplicate.get().getEmployeeID().equals(employeeId)) {
                throw new IllegalArgumentException("Email '" + email + "' is already in use by another employee.");
            }
        }

        existing.setEmployeeName(name.trim());
        existing.setEmail(email.trim().toLowerCase());
        existing.setDesignation(designation.trim());
        existing.setSalary(salary);

        if (role != null && !role.trim().isEmpty()) {
            existing.setRole(role.equalsIgnoreCase("ADMIN") ? "ADMIN" : "EMPLOYEE");
        }

        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            if (rawPassword.length() < 6) {
                throw new IllegalArgumentException("New password must be at least 6 characters long.");
            }
            existing.setPassword(PasswordUtil.hashPassword(rawPassword));
        }

        employeeDAO.update(existing);
    }

    public void deleteEmployee(int employeeId) {
        employeeDAO.delete(employeeId);
    }

    public Optional<Employee> getEmployeeById(int employeeId) {
        return employeeDAO.findById(employeeId);
    }

    public Optional<Employee> getEmployeeWithProjects(int employeeId) {
        return employeeDAO.findByIdWithProjects(employeeId);
    }

    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeDAO.findByEmail(email);
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.findAll();
    }

    public long getTotalEmployeeCount() {
        return employeeDAO.count();
    }

    public Optional<Employee> authenticate(String email, String password) {
        if (email == null || password == null) return Optional.empty();

        Optional<Employee> employeeOpt = employeeDAO.findByEmail(email.trim().toLowerCase());
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            if (PasswordUtil.verifyPassword(password, employee.getPassword())) {
                return Optional.of(employee);
            }
        }
        return Optional.empty();
    }

    private void validateCommonFields(String name, String email, String designation, Double salary) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format (e.g., user@example.com).");
        }
        if (designation == null || designation.trim().isEmpty()) {
            throw new IllegalArgumentException("Designation is required.");
        }
        if (salary == null) {
            throw new IllegalArgumentException("Salary is required.");
        }
        if (salary <= 0) {
            throw new IllegalArgumentException("Salary must be a positive number greater than 0.");
        }
    }
}
