package com.college.controller;

import com.college.config.ThymeleafConfig;
import com.college.model.Employee;
import com.college.service.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MyProjectsServlet extends HttpServlet {

    private EmployeeService employeeService;

    @Override
    public void init() throws ServletException {
        employeeService = new EmployeeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer loggedInId = (Integer) session.getAttribute("userId");
        Optional<Employee> empOpt = employeeService.getEmployeeWithProjects(loggedInId);

        Map<String, Object> model = new HashMap<>();
        model.put("isPersonalView", true);

        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            model.put("employee", emp);
            model.put("myProjects", emp.getProjects());
        } else {
            model.put("errorMessage", "Employee record not found.");
        }

        ThymeleafConfig.render(request, response, "employee-projects", model);
    }
}
