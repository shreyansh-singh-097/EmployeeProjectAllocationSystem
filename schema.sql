-- ============================================================
-- Employee Project Allocation System - Database Schema & Data
-- Copy and run all commands in TablePlus
-- ============================================================

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS employee_project_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE employee_project_db;

-- Step 2: Drop existing tables in reverse dependency order (clean slate)
DROP TABLE IF EXISTS employee_project;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS project;

-- Step 3: Create employee table
CREATE TABLE employee (
    employee_id   INT AUTO_INCREMENT PRIMARY KEY,
    employee_name VARCHAR(100)  NOT NULL,
    email         VARCHAR(100)  NOT NULL UNIQUE,
    designation   VARCHAR(100)  NOT NULL,
    salary        DOUBLE        NOT NULL,
    password      VARCHAR(255)  NOT NULL COMMENT 'PBKDF2WithHmacSHA256 hashed password with 16-byte salt',
    role          VARCHAR(20)   NOT NULL DEFAULT 'EMPLOYEE' COMMENT 'ADMIN or EMPLOYEE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 4: Create project table
CREATE TABLE project (
    project_id        INT AUTO_INCREMENT PRIMARY KEY,
    project_name      VARCHAR(150) NOT NULL,
    client_name       VARCHAR(100) NOT NULL,
    start_date        DATE         NOT NULL,
    budget            DOUBLE       NOT NULL,
    maximum_team_size INT          NOT NULL DEFAULT 5,
    status            VARCHAR(20)  NOT NULL DEFAULT 'Planned' COMMENT 'Planned, Active, or Completed'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 5: Create employee_project join table (Bidirectional Many-to-Many mapping)
CREATE TABLE employee_project (
    employee_id INT NOT NULL,
    project_id  INT NOT NULL,
    PRIMARY KEY (employee_id, project_id),
    CONSTRAINT uk_employee_project UNIQUE (employee_id, project_id),
    CONSTRAINT fk_ep_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_ep_project  FOREIGN KEY (project_id)  REFERENCES project  (project_id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Step 6: Insert Faculty Admins & Student Employees
--
-- Passwords:
-- All Admins   -> AdminPassword123!
-- All Employees -> EmpPassword123!
-- ============================================================

INSERT INTO employee (employee_id, employee_name, email, designation, salary, password, role) VALUES
-- Admins (Faculty / Mentors)
(1, 'Abdul Moid', 'abdul.moid@college.edu', 'Head of Department', 145000.0, 'l2ORT64pVYBOW9XyotcPbw==:XgFOhQLjYNGO8dAtTxH1TVrrASPkIeI6qPZesHhg9R0=', 'ADMIN'),
(2, 'Swapnendu Karmakar', 'swapnendu.karmakar@college.edu', 'Project Director', 140000.0, 'l2ORT64pVYBOW9XyotcPbw==:XgFOhQLjYNGO8dAtTxH1TVrrASPkIeI6qPZesHhg9R0=', 'ADMIN'),

-- Employees (Developers & Engineers)
(3, 'Shreyansh Singh', 'shreyansh.singh@college.edu', 'Full Stack Engineer', 85000.0, 'N9KiyAhagSXuPBdBf2DFUA==:pJdO/dWhNiq6OYmP9XnRqkOLaoIfFsTMxoZkRq8QA7I=', 'EMPLOYEE'),
(4, 'Udit', 'udit@college.edu', 'Backend Engineer', 82000.0, 'N9KiyAhagSXuPBdBf2DFUA==:pJdO/dWhNiq6OYmP9XnRqkOLaoIfFsTMxoZkRq8QA7I=', 'EMPLOYEE'),
(5, 'Anuj', 'anuj@college.edu', 'Frontend Developer', 78000.0, 'N9KiyAhagSXuPBdBf2DFUA==:pJdO/dWhNiq6OYmP9XnRqkOLaoIfFsTMxoZkRq8QA7I=', 'EMPLOYEE'),
(6, 'Vijay', 'vijay@college.edu', 'DevOps & Cloud Engineer', 84000.0, 'N9KiyAhagSXuPBdBf2DFUA==:pJdO/dWhNiq6OYmP9XnRqkOLaoIfFsTMxoZkRq8QA7I=', 'EMPLOYEE'),
(7, 'Aryan', 'aryan@college.edu', 'Mobile App Developer', 79000.0, 'N9KiyAhagSXuPBdBf2DFUA==:pJdO/dWhNiq6OYmP9XnRqkOLaoIfFsTMxoZkRq8QA7I=', 'EMPLOYEE'),
(8, 'Aman', 'aman@college.edu', 'Database Administrator', 80000.0, 'N9KiyAhagSXuPBdBf2DFUA==:pJdO/dWhNiq6OYmP9XnRqkOLaoIfFsTMxoZkRq8QA7I=', 'EMPLOYEE');

-- Step 7: Insert Sample Projects
INSERT INTO project (project_id, project_name, client_name, start_date, budget, maximum_team_size, status) VALUES
(1, 'Campus ERP Modernization', 'Apex University', '2026-01-15', 65000.0, 4, 'Planned'),
(2, 'Smart Attendance System', 'Tech Education Board', '2025-11-01', 45000.0, 4, 'Active'),
(3, 'Online Examination Portal', 'State Tech Board', '2025-08-20', 95000.0, 5, 'Active'),
(4, 'AI Library Archive', 'Digital India Initiative', '2025-04-10', 35000.0, 2, 'Completed');

-- Step 8: Insert Initial Allocations (Many-to-Many associations)
-- Smart Attendance System (Active): Shreyansh, Udit, Vijay (3 of 4 seats)
-- Online Examination Portal (Active): Shreyansh, Anuj (2 of 5 seats)
-- Campus ERP Modernization (Planned): Aryan (1 of 4 seats)
-- AI Library Archive (Completed): Aman (1 of 2 seats)
INSERT INTO employee_project (employee_id, project_id) VALUES
(3, 2), -- Shreyansh -> Smart Attendance System
(4, 2), -- Udit      -> Smart Attendance System
(6, 2), -- Vijay     -> Smart Attendance System
(3, 3), -- Shreyansh -> Online Examination Portal
(5, 3), -- Anuj      -> Online Examination Portal
(7, 1), -- Aryan     -> Campus ERP Modernization
(8, 4); -- Aman      -> AI Library Archive

-- ============================================================
-- Step 9: Verify TablePlus Data
-- ============================================================
SELECT * FROM employee;
SELECT * FROM project;
SELECT * FROM employee_project;
