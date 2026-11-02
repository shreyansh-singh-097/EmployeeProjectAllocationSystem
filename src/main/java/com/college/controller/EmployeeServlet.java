package com.college.controller;

import com.college.config.ThymeleafConfig;
import com.college.model.Employee;
import com.college.service.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmployeeServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServlet.class);
    private EmployeeService employeeService;

    @Override
    public void init() throws ServletException {
        employeeService = new EmployeeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if ("/new".equalsIgnoreCase(pathInfo)) {
            showForm(request, response, new Employee(), false);
            return;
        }

        if ("/edit".equalsIgnoreCase(pathInfo)) {
            handleEditGet(request, response);
            return;
        }

        List<Employee> list = employeeService.getAllEmployees();
        Map<String, Object> model = new HashMap<>();
        model.put("employees", list);
        ThymeleafConfig.render(request, response, "employee-list", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if ("/delete".equalsIgnoreCase(pathInfo)) {
            handleDelete(request, response);
        } else {
            handleSave(request, response);
        }
    }

    private void handleEditGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            forwardToError(request, response, "Employee ID is required.", 400);
            return;
        }
        try {
            int id = Integer.parseInt(idStr);
            Optional<Employee> opt = employeeService.getEmployeeById(id);
            if (opt.isPresent()) {
                Employee emp = opt.get();
                emp.setPassword("");
                showForm(request, response, emp, true);
            } else {
                forwardToError(request, response, "Employee not found: " + id, 404);
            }
        } catch (NumberFormatException e) {
            forwardToError(request, response, "Invalid Employee ID: " + idStr, 400);
        }
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("employeeID");
        String name = request.getParameter("employeeName");
        String email = request.getParameter("email");
        String designation = request.getParameter("designation");
        String salaryStr = request.getParameter("salary");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        boolean isEdit = (idStr != null && !idStr.trim().isEmpty());

        Employee formEmp = new Employee();
        formEmp.setEmployeeName(name);
        formEmp.setEmail(email);
        formEmp.setDesignation(designation);
        formEmp.setRole(role);

        Double salary = null;
        try {
            if (salaryStr != null && !salaryStr.isBlank()) {
                salary = Double.parseDouble(salaryStr);
                formEmp.setSalary(salary);
            }
        } catch (NumberFormatException e) {
            showFormWithError(request, response, formEmp, isEdit, "Salary must be a valid number.");
            return;
        }

        try {
            if (isEdit) {
                int id = Integer.parseInt(idStr);
                formEmp.setEmployeeID(id);
                employeeService.updateEmployee(id, name, email, designation, salary, password, role);
                logger.info("Employee updated: {}", id);
                response.sendRedirect(request.getContextPath() + "/employees?success=" +
                        encode("Employee updated successfully."));
            } else {
                employeeService.createEmployee(name, email, designation, salary, password, role);
                logger.info("Employee created: {}", email);
                response.sendRedirect(request.getContextPath() + "/employees?success=" +
                        encode("Employee created successfully."));
            }
        } catch (IllegalArgumentException ex) {
            showFormWithError(request, response, formEmp, isEdit, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error saving employee: {}", ex.getMessage(), ex);
            forwardToError(request, response, "Server error: " + ex.getMessage(), 500);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("employeeID");
        if (idStr == null || idStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/employees?error=" +
                    encode("Employee ID missing."));
            return;
        }
        try {
            int id = Integer.parseInt(idStr);
            employeeService.deleteEmployee(id);
            logger.info("Employee deleted: {}", id);
            response.sendRedirect(request.getContextPath() + "/employees?success=" +
                    encode("Employee deleted successfully."));
        } catch (Exception e) {
            logger.error("Error deleting employee: {}", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/employees?error=" +
                    encode("Could not delete employee: " + e.getMessage()));
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Employee employee, boolean isEdit) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("employee", employee);
        model.put("isEdit", isEdit);
        ThymeleafConfig.render(request, response, "employee-form", model);
    }

    private void showFormWithError(HttpServletRequest request, HttpServletResponse response,
                                   Employee employee, boolean isEdit, String error) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("employee", employee);
        model.put("isEdit", isEdit);
        model.put("errorMessage", error);
        ThymeleafConfig.render(request, response, "employee-form", model);
    }

    private void forwardToError(HttpServletRequest request, HttpServletResponse response,
                                String message, int code) throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.setAttribute("errorCode", code);
        request.getRequestDispatcher("/error").forward(request, response);
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
