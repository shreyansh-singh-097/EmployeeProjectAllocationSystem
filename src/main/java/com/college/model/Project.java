package com.college.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "project")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectID;

    @Column(name = "project_name", nullable = false, length = 150)
    private String projectName;

    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "budget", nullable = false)
    private Double budget;

    @Column(name = "maximum_team_size", nullable = false)
    private Integer maximumTeamSize;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    private Set<Employee> employees = new HashSet<>();

    public Project() {
        this.status = "Planned";
        this.maximumTeamSize = 5;
    }

    public Project(String projectName, String clientName, LocalDate startDate, Double budget, Integer maximumTeamSize, String status) {
        this.projectName = projectName;
        this.clientName = clientName;
        this.startDate = startDate;
        this.budget = budget;
        this.maximumTeamSize = maximumTeamSize;
        this.status = status != null ? status : "Planned";
    }

    public void addEmployee(Employee employee) {
        if (employee != null) {
            this.employees.add(employee);
            employee.getProjects().add(this);
        }
    }

    public void removeEmployee(Employee employee) {
        if (employee != null) {
            this.employees.remove(employee);
            employee.getProjects().remove(this);
        }
    }

    public int getCurrentTeamSize() {
        return employees != null ? employees.size() : 0;
    }

    public int getRemainingCapacity() {
        if (maximumTeamSize == null) return 0;
        return Math.max(0, maximumTeamSize - getCurrentTeamSize());
    }

    public boolean isFull() {
        if (maximumTeamSize == null) return false;
        return getCurrentTeamSize() >= maximumTeamSize;
    }

    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(this.status);
    }

    public boolean isAllocationAllowed() {
        return !isCompleted() && !isFull();
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public Integer getProjectId() {
        return projectID;
    }

    public void setProjectId(Integer projectId) {
        this.projectID = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Integer getMaximumTeamSize() {
        return maximumTeamSize;
    }

    public void setMaximumTeamSize(Integer maximumTeamSize) {
        this.maximumTeamSize = maximumTeamSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        if (projectID != null && project.projectID != null) {
            return Objects.equals(projectID, project.projectID);
        }
        return Objects.equals(projectName, project.projectName);
    }

    @Override
    public int hashCode() {
        return projectID != null ? Objects.hash(projectID) : Objects.hash(projectName);
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectID=" + projectID +
                ", projectName='" + projectName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", startDate=" + startDate +
                ", budget=" + budget +
                ", maximumTeamSize=" + maximumTeamSize +
                ", status='" + status + '\'' +
                '}';
    }
}
