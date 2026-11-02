package com.college.service;

import com.college.dao.EmployeeDAO;
import com.college.dao.ProjectDAO;
import com.college.model.Employee;
import com.college.model.Project;
import com.college.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AllocationService {

    private static final Logger logger = LoggerFactory.getLogger(AllocationService.class);

    private final EmployeeDAO employeeDAO;
    private final ProjectDAO projectDAO;

    public AllocationService() {
        this.employeeDAO = new EmployeeDAO();
        this.projectDAO = new ProjectDAO();
    }

    public AllocationService(EmployeeDAO employeeDAO, ProjectDAO projectDAO) {
        this.employeeDAO = employeeDAO;
        this.projectDAO = projectDAO;
    }

    public List<String> validateAllocationRequest(int employeeId, List<Integer> projectIds) {
        List<String> errors = new ArrayList<>();

        if (employeeId <= 0) {
            errors.add("Please select a valid employee.");
            return errors;
        }

        if (projectIds == null || projectIds.isEmpty()) {
            errors.add("Please select at least one project for allocation.");
            return errors;
        }

        var employeeOpt = employeeDAO.findByIdWithProjects(employeeId);
        if (employeeOpt.isEmpty()) {
            errors.add("Selected employee does not exist.");
            return errors;
        }

        Employee employee = employeeOpt.get();

        for (Integer pid : projectIds) {
            var projectOpt = projectDAO.findByIdWithEmployees(pid);
            if (projectOpt.isEmpty()) {
                errors.add("Project with ID " + pid + " does not exist.");
                continue;
            }

            Project project = projectOpt.get();

            if (project.isCompleted()) {
                errors.add("Project '" + project.getProjectName() + "' is Completed. No new allocations are permitted.");
            }

            if (employee.getProjects().contains(project) || project.getEmployees().contains(employee)) {
                errors.add("Employee '" + employee.getEmployeeName() + "' is already assigned to project '" + project.getProjectName() + "'.");
            }

            if (project.getCurrentTeamSize() >= project.getMaximumTeamSize()) {
                errors.add("Project '" + project.getProjectName() + "' has reached its maximum team capacity (" +
                        project.getMaximumTeamSize() + ").");
            }
        }

        return errors;
    }

    public void confirmAndSaveAllocation(int employeeId, List<Integer> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            throw new IllegalArgumentException("No projects selected for allocation.");
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Employee employee = session.createQuery(
                    "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects WHERE e.employeeID = :id", Employee.class)
                    .setParameter("id", employeeId)
                    .uniqueResult();

            if (employee == null) {
                throw new IllegalStateException("Employee with ID " + employeeId + " not found.");
            }

            for (Integer projectId : projectIds) {
                Project project = session.createQuery(
                        "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.employees WHERE p.projectID = :id", Project.class)
                        .setParameter("id", projectId)
                        .uniqueResult();

                if (project == null) {
                    throw new IllegalStateException("Project with ID " + projectId + " not found.");
                }

                if (project.isCompleted()) {
                    throw new IllegalStateException("Project '" + project.getProjectName() + "' is Completed. Cannot allocate.");
                }

                if (project.getCurrentTeamSize() >= project.getMaximumTeamSize()) {
                    throw new IllegalStateException("Project '" + project.getProjectName() + "' has already reached maximum team capacity (" + project.getMaximumTeamSize() + ").");
                }

                if (employee.getProjects().contains(project)) {
                    throw new IllegalStateException("Employee '" + employee.getEmployeeName() + "' is already assigned to project '" + project.getProjectName() + "'.");
                }

                employee.addProject(project);
                session.merge(project);
            }

            session.merge(employee);
            tx.commit();
            logger.info("Successfully allocated employee {} to projects {}", employeeId, projectIds);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Transaction rolled back for allocation of employee {}: {}", employeeId, e.getMessage(), e);
            throw new RuntimeException("Allocation failed: " + e.getMessage(), e);
        }
    }

    public void deallocate(int employeeId, int projectId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Employee employee = session.createQuery(
                    "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects WHERE e.employeeID = :id", Employee.class)
                    .setParameter("id", employeeId)
                    .uniqueResult();

            Project project = session.createQuery(
                    "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.employees WHERE p.projectID = :id", Project.class)
                    .setParameter("id", projectId)
                    .uniqueResult();

            if (employee != null && project != null) {
                employee.removeProject(project);
                session.merge(project);
                session.merge(employee);
                logger.info("Successfully removed employee {} from project {}", employeeId, projectId);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Failed to remove allocation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove allocation: " + e.getMessage(), e);
        }
    }
}
