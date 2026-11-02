package com.college.controller;

import com.college.config.ThymeleafConfig;
import com.college.model.Employee;
import com.college.model.Project;
import com.college.service.AllocationService;
import com.college.service.EmployeeService;
import com.college.service.ProjectService;
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
import java.util.*;

public class AllocationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AllocationServlet.class);

    private static final String SESS_EMP_ID   = "pending_employee_id";
    private static final String SESS_PROJ_IDS = "pending_project_ids";

    private AllocationService allocationService;
    private EmployeeService employeeService;
    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
        allocationService = new AllocationService();
        employeeService = new EmployeeService();
        projectService = new ProjectService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if ("/new".equalsIgnoreCase(pathInfo)) {
            showAllocationForm(request, response, null, Collections.emptyList(), null);
            return;
        }

        if ("/review".equalsIgnoreCase(pathInfo)) {
            showReviewPage(request, response);
            return;
        }

        showOverview(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if ("/stage".equalsIgnoreCase(pathInfo)) {
            handleStage(request, response);
        } else if ("/confirm".equalsIgnoreCase(pathInfo)) {
            handleConfirm(request, response);
        } else if ("/cancel".equalsIgnoreCase(pathInfo)) {
            handleCancel(request, response);
        } else if ("/remove".equalsIgnoreCase(pathInfo)) {
            handleRemove(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/allocations");
        }
    }

    private void showAllocationForm(HttpServletRequest request, HttpServletResponse response,
                                    Integer selEmpId, List<Integer> selProjIds, String error)
            throws IOException {

        Map<String, Object> model = new HashMap<>();
        model.put("employees", employeeService.getAllEmployees());
        model.put("projects", projectService.getAllProjects());
        model.put("selectedEmployeeId", selEmpId);
        model.put("selectedProjectIds", selProjIds != null ? selProjIds : Collections.emptyList());
        if (error != null) model.put("errorMessage", error);

        ThymeleafConfig.render(request, response, "allocation-form", model);
    }

    private void handleStage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String empIdStr = request.getParameter("employeeId");
        String[] projStrs = request.getParameterValues("projectIds");

        Integer empId = null;
        try {
            if (empIdStr != null) empId = Integer.parseInt(empIdStr);
        } catch (NumberFormatException ignored) {}

        List<Integer> projIds = new ArrayList<>();
        if (projStrs != null) {
            for (String s : projStrs) {
                try {
                    projIds.add(Integer.parseInt(s));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (empId == null) {
            showAllocationForm(request, response, null, projIds, "Please select an employee.");
            return;
        }
        if (projIds.isEmpty()) {
            showAllocationForm(request, response, empId, projIds, "Please select at least one project.");
            return;
        }

        List<String> errors = allocationService.validateAllocationRequest(empId, projIds);
        if (!errors.isEmpty()) {
            showAllocationForm(request, response, empId, projIds, String.join("<br/>", errors));
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SESS_EMP_ID, empId);
        session.setAttribute(SESS_PROJ_IDS, projIds);

        logger.info("Staged allocation in session: empId={} projIds={}", empId, projIds);
        response.sendRedirect(request.getContextPath() + "/allocations/review");
    }

    private void showReviewPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESS_EMP_ID) == null) {
            response.sendRedirect(request.getContextPath() + "/allocations/new?info=" +
                    encode("No pending allocation. Please make a selection."));
            return;
        }

        Integer empId = (Integer) session.getAttribute(SESS_EMP_ID);
        @SuppressWarnings("unchecked")
        List<Integer> projIds = (List<Integer>) session.getAttribute(SESS_PROJ_IDS);

        Optional<Employee> empOpt = employeeService.getEmployeeWithProjects(empId);
        if (empOpt.isEmpty()) {
            clearPendingAllocation(session);
            response.sendRedirect(request.getContextPath() + "/allocations/new?error=" +
                    encode("Selected employee no longer exists."));
            return;
        }

        List<Project> selectedProjects = new ArrayList<>();
        for (Integer pid : projIds) {
            projectService.getProjectWithEmployees(pid).ifPresent(selectedProjects::add);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("employee", empOpt.get());
        model.put("selectedProjects", selectedProjects);

        ThymeleafConfig.render(request, response, "allocation-review", model);
    }

    private void handleConfirm(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESS_EMP_ID) == null) {
            response.sendRedirect(request.getContextPath() + "/allocations/new?error=" +
                    encode("Session expired. Please start the allocation again."));
            return;
        }

        Integer empId = (Integer) session.getAttribute(SESS_EMP_ID);
        @SuppressWarnings("unchecked")
        List<Integer> projIds = (List<Integer>) session.getAttribute(SESS_PROJ_IDS);

        try {
            allocationService.confirmAndSaveAllocation(empId, projIds);
            clearPendingAllocation(session);
            logger.info("Allocation confirmed: empId={} projIds={}", empId, projIds);
            response.sendRedirect(request.getContextPath() + "/allocations?success=" +
                    encode("Project allocation saved successfully!"));
        } catch (Exception ex) {
            logger.error("Allocation commit failed: {}", ex.getMessage(), ex);
            response.sendRedirect(request.getContextPath() + "/allocations/review?error=" +
                    encode("Allocation failed: " + ex.getMessage()));
        }
    }

    private void handleCancel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) clearPendingAllocation(session);

        logger.info("Allocation cancelled by user.");
        response.sendRedirect(request.getContextPath() + "/allocations/new?info=" +
                    encode("Allocation cancelled. You can start a new allocation below."));
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String empStr = request.getParameter("employeeId");
        String projStr = request.getParameter("projectId");

        if (empStr == null || projStr == null) {
            response.sendRedirect(request.getContextPath() + "/allocations?error=" +
                    encode("Missing employee or project ID."));
            return;
        }
        try {
            allocationService.deallocate(Integer.parseInt(empStr), Integer.parseInt(projStr));
            response.sendRedirect(request.getContextPath() + "/allocations?success=" +
                    encode("Employee removed from project successfully."));
        } catch (Exception e) {
            logger.error("Remove allocation error: {}", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/allocations?error=" +
                    encode("Could not remove allocation: " + e.getMessage()));
        }
    }

    private void showOverview(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Map<String, Object> model = new HashMap<>();
        model.put("projects", projectService.getAllProjects());
        model.put("employees", employeeService.getAllEmployees());
        ThymeleafConfig.render(request, response, "employee-projects", model);
    }

    private void clearPendingAllocation(HttpSession session) {
        session.removeAttribute(SESS_EMP_ID);
        session.removeAttribute(SESS_PROJ_IDS);
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
