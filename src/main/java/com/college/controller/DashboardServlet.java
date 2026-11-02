package com.college.controller;

import com.college.config.ThymeleafConfig;
import com.college.dao.EmployeeDAO;
import com.college.dao.ProjectDAO;
import com.college.model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DashboardServlet extends HttpServlet {

    private EmployeeDAO employeeDAO;
    private ProjectDAO projectDAO;

    @Override
    public void init() throws ServletException {
        employeeDAO = new EmployeeDAO();
        projectDAO = new ProjectDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("totalEmployees", employeeDAO.count());
        model.put("totalProjects", projectDAO.count());
        model.put("activeProjects", projectDAO.countActive());

        Integer userId = (Integer) session.getAttribute("userId");
        Optional<Employee> empOpt = employeeDAO.findByIdWithProjects(userId);
        empOpt.ifPresent(e -> model.put("myProjectsCount", e.getProjects().size()));

        ThymeleafConfig.render(request, response, "dashboard", model);
    }
}
