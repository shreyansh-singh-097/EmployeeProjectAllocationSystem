package com.college.controller;

import com.college.config.ThymeleafConfig;
import com.college.model.Employee;
import com.college.service.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private EmployeeService employeeService;

    @Override
    public void init() throws ServletException {
        employeeService = new EmployeeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        if (request.getParameter("error") != null) {
            model.put("errorMessage", request.getParameter("error"));
        }
        if ("true".equalsIgnoreCase(request.getParameter("logout"))) {
            model.put("successMessage", "You have been logged out successfully.");
        }

        ThymeleafConfig.render(request, response, "login", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/login?error=" +
                    encode("Please enter both email and password."));
            return;
        }

        Optional<Employee> userOpt = employeeService.authenticate(email.trim(), password);

        if (userOpt.isPresent()) {
            Employee user = userOpt.get();

            HttpSession old = request.getSession(false);
            if (old != null) old.invalidate();

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getEmployeeID());
            session.setAttribute("userName", user.getEmployeeName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());

            logger.info("User logged in: {}", user.getEmail());
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            logger.warn("Invalid login for: {}", email);
            response.sendRedirect(request.getContextPath() + "/login?error=" +
                    encode("Invalid email or password."));
        }
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
