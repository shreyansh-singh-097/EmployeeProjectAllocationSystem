package com.college.service;

import com.college.dao.ProjectDAO;
import com.college.model.Project;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProjectService {

    private static final List<String> VALID_STATUSES = Arrays.asList("Planned", "Active", "Completed");
    private final ProjectDAO projectDAO;

    public ProjectService() {
        this.projectDAO = new ProjectDAO();
    }

    public ProjectService(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public void createProject(String projectName, String clientName, LocalDate startDate, Double budget, Integer maxTeamSize, String status) {
        validateProjectFields(projectName, clientName, startDate, budget, maxTeamSize, status);

        Project project = new Project(projectName.trim(), clientName.trim(), startDate, budget, maxTeamSize, normalizeStatus(status));
        projectDAO.save(project);
    }

    public void updateProject(int projectId, String projectName, String clientName, LocalDate startDate, Double budget, Integer maxTeamSize, String status) {
        validateProjectFields(projectName, clientName, startDate, budget, maxTeamSize, status);

        Project existing = projectDAO.findByIdWithEmployees(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        if (maxTeamSize < existing.getCurrentTeamSize()) {
            throw new IllegalArgumentException("Maximum team size (" + maxTeamSize + 
                    ") cannot be less than the current allocated team size (" + existing.getCurrentTeamSize() + ").");
        }

        existing.setProjectName(projectName.trim());
        existing.setClientName(clientName.trim());
        existing.setStartDate(startDate);
        existing.setBudget(budget);
        existing.setMaximumTeamSize(maxTeamSize);
        existing.setStatus(normalizeStatus(status));

        projectDAO.update(existing);
    }

    public void deleteProject(int projectId) {
        projectDAO.delete(projectId);
    }

    public Optional<Project> getProjectById(int projectId) {
        return projectDAO.findById(projectId);
    }

    public Optional<Project> getProjectWithEmployees(int projectId) {
        return projectDAO.findByIdWithEmployees(projectId);
    }

    public List<Project> getAllProjects() {
        return projectDAO.findAll();
    }

    public long getTotalProjectCount() {
        return projectDAO.count();
    }

    public long getActiveProjectCount() {
        return projectDAO.countActive();
    }

    private void validateProjectFields(String projectName, String clientName, LocalDate startDate, Double budget, Integer maxTeamSize, String status) {
        if (projectName == null || projectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required.");
        }
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new IllegalArgumentException("Client name is required.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date is required.");
        }
        if (budget == null) {
            throw new IllegalArgumentException("Budget is required.");
        }
        if (budget <= 0) {
            throw new IllegalArgumentException("Budget must be greater than 0.");
        }
        if (maxTeamSize == null) {
            throw new IllegalArgumentException("Maximum team size is required.");
        }
        if (maxTeamSize < 1) {
            throw new IllegalArgumentException("Maximum team size must be at least 1.");
        }
        if (status == null || !VALID_STATUSES.contains(normalizeStatus(status))) {
            throw new IllegalArgumentException("Status must be one of: Planned, Active, or Completed.");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null) return "Planned";
        for (String valid : VALID_STATUSES) {
            if (valid.equalsIgnoreCase(status.trim())) {
                return valid;
            }
        }
        return status.trim();
    }
}
