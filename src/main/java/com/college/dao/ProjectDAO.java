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

public class ProjectDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProjectDAO.class);

    public void save(Project project) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(project);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Failed to save project: {}", e.getMessage(), e);
            throw new RuntimeException("Database error saving project: " + e.getMessage(), e);
        }
    }

    public void update(Project project) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(project);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Failed to update project: {}", e.getMessage(), e);
            throw new RuntimeException("Database error updating project: " + e.getMessage(), e);
        }
    }

    public void delete(int projectId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Project project = session.createQuery(
                    "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.employees WHERE p.projectID = :id", Project.class)
                    .setParameter("id", projectId)
                    .uniqueResult();

            if (project != null) {
                for (Employee emp : project.getEmployees()) {
                    emp.getProjects().remove(project);
                }
                project.getEmployees().clear();
                session.remove(project);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Failed to delete project with ID {}: {}", projectId, e.getMessage(), e);
            throw new RuntimeException("Database error deleting project: " + e.getMessage(), e);
        }
    }

    public Optional<Project> findById(int projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Project proj = session.get(Project.class, projectId);
            return Optional.ofNullable(proj);
        } catch (Exception e) {
            logger.error("Error finding project by ID {}: {}", projectId, e.getMessage(), e);
            throw new RuntimeException("Database error fetching project: " + e.getMessage(), e);
        }
    }

    public Optional<Project> findByIdWithEmployees(int projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Project proj = session.createQuery(
                    "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.employees WHERE p.projectID = :id", Project.class)
                    .setParameter("id", projectId)
                    .uniqueResult();
            return Optional.ofNullable(proj);
        } catch (Exception e) {
            logger.error("Error finding project with employees by ID {}: {}", projectId, e.getMessage(), e);
            throw new RuntimeException("Database error fetching project with employees: " + e.getMessage(), e);
        }
    }

    public List<Project> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.employees ORDER BY p.projectID ASC", Project.class)
                    .list();
        } catch (Exception e) {
            logger.error("Error finding all projects: {}", e.getMessage(), e);
            throw new RuntimeException("Database error retrieving project list: " + e.getMessage(), e);
        }
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(p) FROM Project p", Long.class).uniqueResult();
        } catch (Exception e) {
            logger.error("Error counting projects: {}", e.getMessage(), e);
            return 0;
        }
    }

    public long countActive() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT COUNT(p) FROM Project p WHERE p.status = 'Active'", Long.class).uniqueResult();
        } catch (Exception e) {
            logger.error("Error counting active projects: {}", e.getMessage(), e);
            return 0;
        }
    }
}
