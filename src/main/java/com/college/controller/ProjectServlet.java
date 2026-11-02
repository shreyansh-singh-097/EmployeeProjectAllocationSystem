package com.college.controller;

import com.college.config.ThymeleafConfig;
import com.college.model.Project;
import com.college.service.ProjectService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProjectServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServlet.class);
    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
        projectService = new ProjectService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if ("/new".equalsIgnoreCase(pathInfo)) {
            Project p = new Project();
            p.setStartDate(LocalDate.now());
            showForm(request, response, p, false);
            return;
        }

        if ("/edit".equalsIgnoreCase(pathInfo)) {
            handleEditGet(request, response);
            return;
        }

        List<Project> list = projectService.getAllProjects();
        Map<String, Object> model = new HashMap<>();
        model.put("projects", list);
        ThymeleafConfig.render(request, response, "project-list", model);
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
            forwardToError(request, response, "Project ID is required.", 400);
            return;
        }
        try {
            int id = Integer.parseInt(idStr);
            Optional<Project> opt = projectService.getProjectWithEmployees(id);
            if (opt.isPresent()) {
                showForm(request, response, opt.get(), true);
            } else {
                forwardToError(request, response, "Project not found: " + id, 404);
            }
        } catch (NumberFormatException e) {
            forwardToError(request, response, "Invalid Project ID: " + idStr, 400);
        }
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("projectID");
        String projectName = request.getParameter("projectName");
        String clientName = request.getParameter("clientName");
        String startStr = request.getParameter("startDate");
        String budgetStr = request.getParameter("budget");
        String maxStr = request.getParameter("maximumTeamSize");
        String status = request.getParameter("status");

        boolean isEdit = (idStr != null && !idStr.trim().isEmpty());

        Project formProj = new Project();
        formProj.setProjectName(projectName);
        formProj.setClientName(clientName);
        formProj.setStatus(status);

        LocalDate startDate = null;
        try {
            if (startStr != null && !startStr.isBlank()) {
                startDate = LocalDate.parse(startStr);
                formProj.setStartDate(startDate);
            }
        } catch (DateTimeParseException e) {
            showFormWithError(request, response, formProj, isEdit, "Start date must be a valid date (YYYY-MM-DD).");
            return;
        }

        Double budget = null;
        try {
            if (budgetStr != null && !budgetStr.isBlank()) {
                budget = Double.parseDouble(budgetStr);
                formProj.setBudget(budget);
            }
        } catch (NumberFormatException e) {
            showFormWithError(request, response, formProj, isEdit, "Budget must be a valid number.");
            return;
        }

        Integer maxTeam = null;
        try {
            if (maxStr != null && !maxStr.isBlank()) {
                maxTeam = Integer.parseInt(maxStr);
                formProj.setMaximumTeamSize(maxTeam);
            }
        } catch (NumberFormatException e) {
            showFormWithError(request, response, formProj, isEdit, "Maximum team size must be a number.");
            return;
        }

        try {
            if (isEdit) {
                int id = Integer.parseInt(idStr);
                formProj.setProjectID(id);
                projectService.updateProject(id, projectName, clientName, startDate, budget, maxTeam, status);
                logger.info("Project updated: {}", id);
                response.sendRedirect(request.getContextPath() + "/projects?success=" +
                        encode("Project updated successfully."));
            } else {
                projectService.createProject(projectName, clientName, startDate, budget, maxTeam, status);
                logger.info("Project created: {}", projectName);
                response.sendRedirect(request.getContextPath() + "/projects?success=" +
                        encode("Project created successfully."));
            }
        } catch (IllegalArgumentException ex) {
            showFormWithError(request, response, formProj, isEdit, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error saving project: {}", ex.getMessage(), ex);
            forwardToError(request, response, "Server error: " + ex.getMessage(), 500);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("projectID");
        if (idStr == null || idStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/projects?error=" +
                    encode("Project ID missing."));
            return;
        }
        try {
            projectService.deleteProject(Integer.parseInt(idStr));
            logger.info("Project deleted: {}", idStr);
            response.sendRedirect(request.getContextPath() + "/projects?success=" +
                    encode("Project deleted successfully."));
        } catch (Exception e) {
            logger.error("Error deleting project: {}", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/projects?error=" +
                    encode("Could not delete project: " + e.getMessage()));
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Project project, boolean isEdit) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("isEdit", isEdit);
        ThymeleafConfig.render(request, response, "project-form", model);
    }

    private void showFormWithError(HttpServletRequest request, HttpServletResponse response,
                                   Project project, boolean isEdit, String error) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("isEdit", isEdit);
        model.put("errorMessage", error);
        ThymeleafConfig.render(request, response, "project-form", model);
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
