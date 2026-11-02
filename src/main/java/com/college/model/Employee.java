package com.college.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "employee")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "employee_id")
    private Integer employeeID;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "salary", nullable = false)
    private Double salary;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "employee_project",
        joinColumns = @JoinColumn(name = "employee_id", referencedColumnName = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id", referencedColumnName = "project_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_employee_project", columnNames = {"employee_id", "project_id"})
    )
    private Set<Project> projects = new HashSet<>();

    public Employee() {
        this.role = "EMPLOYEE";
    }

    public Employee(String employeeName, String email, String designation, Double salary, String password, String role) {
        this.employeeName = employeeName;
        this.email = email;
        this.designation = designation;
        this.salary = salary;
        this.password = password;
        this.role = role != null ? role : "EMPLOYEE";
    }

    public void addProject(Project project) {
        if (project != null) {
            this.projects.add(project);
            project.getEmployees().add(this);
        }
    }

    public void removeProject(Project project) {
        if (project != null) {
            this.projects.remove(project);
            project.getEmployees().remove(this);
        }
    }

    public Integer getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Integer employeeID) {
        this.employeeID = employeeID;
    }

    public Integer getEmployeeId() {
        return employeeID;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeID = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        if (employeeID != null && employee.employeeID != null) {
            return Objects.equals(employeeID, employee.employeeID);
        }
        return Objects.equals(email, employee.email);
    }

    @Override
    public int hashCode() {
        return employeeID != null ? Objects.hash(employeeID) : Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeID=" + employeeID +
                ", employeeName='" + employeeName + '\'' +
                ", email='" + email + '\'' +
                ", designation='" + designation + '\'' +
                ", salary=" + salary +
                ", role='" + role + '\'' +
                '}';
    }
}
