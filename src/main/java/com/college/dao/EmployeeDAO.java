package com.college.dao;

import com.college.model.Employee;
import com.college.model.Project;
import com.college.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class EmployeeDAO {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDAO.class);

    public void save(Employee employee) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(employee);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Failed to save employee: {}", e.getMessage(), e);
            throw new RuntimeException("Database error saving employee: " + e.getMessage(), e);
        }
    }

    public void update(Employee employee) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(employee);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Failed to update employee: {}", e.getMessage(), e);
            throw new RuntimeException("Database error updating employee: " + e.getMessage(), e);
        }
    }

    public void delete(int employeeId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee employee = session.createQuery(
                    "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects WHERE e.employeeID = :id", Employee.class)
                    .setParameter("id", employeeId)
                    .uniqueResult();

            if (employee != null) {
                for (Project project : employee.getProjects()) {
                    project.getEmployees().remove(employee);
                }
                employee.getProjects().clear();
                session.remove(employee);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Failed to delete employee with ID {}: {}", employeeId, e.getMessage(), e);
            throw new RuntimeException("Database error deleting employee: " + e.getMessage(), e);
        }
    }

    public Optional<Employee> findById(int employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Employee emp = session.get(Employee.class, employeeId);
            return Optional.ofNullable(emp);
        } catch (Exception e) {
            logger.error("Error finding employee by ID {}: {}", employeeId, e.getMessage(), e);
            throw new RuntimeException("Database error fetching employee: " + e.getMessage(), e);
        }
    }

    public Optional<Employee> findByIdWithProjects(int employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Employee emp = session.createQuery(
                    "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects WHERE e.employeeID = :id", Employee.class)
                    .setParameter("id", employeeId)
                    .uniqueResult();
            return Optional.ofNullable(emp);
        } catch (Exception e) {
            logger.error("Error finding employee with projects by ID {}: {}", employeeId, e.getMessage(), e);
            throw new RuntimeException("Database error fetching employee with projects: " + e.getMessage(), e);
        }
    }

    public Optional<Employee> findByEmail(String email) {
        if (email == null) return Optional.empty();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Employee emp = session.createQuery(
                    "SELECT e FROM Employee e WHERE LOWER(e.email) = LOWER(:email)", Employee.class)
                    .setParameter("email", email.trim())
                    .uniqueResult();
            return Optional.ofNullable(emp);
        } catch (Exception e) {
            logger.error("Error finding employee by email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Database error looking up employee: " + e.getMessage(), e);
        }
    }

    public List<Employee> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects ORDER BY e.employeeID ASC", Employee.class)
                    .list();
        } catch (Exception e) {
            logger.error("Error finding all employees: {}", e.getMessage(), e);
            throw new RuntimeException("Database error retrieving employee list: " + e.getMessage(), e);
        }
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(e) FROM Employee e", Long.class).uniqueResult();
        } catch (Exception e) {
            logger.error("Error counting employees: {}", e.getMessage(), e);
            return 0;
        }
    }
}
