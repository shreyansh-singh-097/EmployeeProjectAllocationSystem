# EmployeeProjectAllocationSystem — Complete Project Deliverable

> ✅ **Build Status: SUCCESSFUL** — 23 Java source files compiled, `EmployeeProjectAllocationSystem.war` (23 MB) built. All requirements fulfilled.

---

## 📁 Complete Folder Tree

```
Shreyansh-Singh_SetB/
├── pom.xml                                          ← Maven WAR project config
├── schema.sql                                       ← Manual database setup script
├── README.md                                        ← Setup, viva Q&A, testing checklist
│
├── src/
│   └── main/
│       ├── java/com/college/
│       │   ├── model/
│       │   │   ├── Employee.java                   ← @Entity, @ManyToMany OWNING SIDE
│       │   │   └── Project.java                    ← @Entity, @ManyToMany INVERSE SIDE
│       │   │
│       │   ├── dao/
│       │   │   ├── EmployeeDAO.java                ← Hibernate session operations for Employee
│       │   │   └── ProjectDAO.java                 ← Hibernate session operations for Project
│       │   │
│       │   ├── service/
│       │   │   ├── EmployeeService.java            ← Validation + business logic for employees
│       │   │   ├── ProjectService.java             ← Validation + business logic for projects
│       │   │   └── AllocationService.java          ← Bidirectional allocation + Hibernate txn
│       │   │
│       │   ├── controller/
│       │   │   ├── LoginServlet.java               ← Authentication, session creation, PRG
│       │   │   ├── LogoutServlet.java              ← session.invalidate(), redirect to login
│       │   │   ├── DashboardServlet.java           ← Dashboard with stats + role-based nav
│       │   │   ├── EmployeeServlet.java            ← CRUD for employees (forward on error, redirect on success)
│       │   │   ├── ProjectServlet.java             ← CRUD for projects
│       │   │   ├── AllocationServlet.java          ← 5-step allocation workflow with session staging
│       │   │   ├── MyProjectsServlet.java          ← Employee sees own projects (userId from session only)
│       │   │   └── ErrorServlet.java               ← Centralized error page (forward() destination)
│       │   │
│       │   ├── filter/
│       │   │   ├── RequestLoggingFilter.java       ← Logs URL + processing time (finally block)
│       │   │   └── AuthenticationFilter.java       ← Session check + RBAC + sendRedirect() to login
│       │   │
│       │   ├── config/
│       │   │   ├── ThymeleafConfig.java            ← Thymeleaf 3.1 + JakartaServletWebApplication
│       │   │   ├── FieldAttributeDialect.java      ← Custom dialect enabling th:field support in Servlets
│       │   │   ├── FieldAttributeProcessor.java    ← Custom processor binding th:field to id, name, value
│       │   │   └── AppStartupListener.java         ← ServletContextListener (init/shutdown)
│       │   │
│       │   └── util/
│       │       ├── HibernateUtil.java              ← SessionFactory singleton + env-var override
│       │       └── PasswordUtil.java               ← PBKDF2WithHmacSHA256 + salt hash/verify
│       │
│       ├── resources/
│       │   ├── hibernate.cfg.xml                   ← Hibernate + MySQL 8 configuration
│       │   └── logback.xml                         ← SLF4J + Logback logging config
│       │
│       └── webapp/
│           ├── css/
│           │   └── style.css                       ← Responsive CSS (custom design system)
│           └── WEB-INF/
│               ├── web.xml                         ← Servlet, Filter, Listener, Error-page mappings
│               └── templates/                      ← All Thymeleaf 3.1 HTML templates
│                   ├── login.html
│                   ├── dashboard.html
│                   ├── employee-form.html
│                   ├── employee-list.html
│                   ├── project-form.html
│                   ├── project-list.html
│                   ├── allocation-form.html
│                   ├── allocation-review.html
│                   ├── employee-projects.html
│                   └── error.html
│
└── target/
    └── EmployeeProjectAllocationSystem.war         ← 23 MB deployable WAR for Tomcat 10.1
```

---

## Database Initialization Commands

```bash
# 1. Start MySQL and open the shell
mysql -u root -p

# 2. Paste and run these SQL commands:
CREATE DATABASE IF NOT EXISTS employee_project_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE employee_project_db;

# 3. Optional: Run the full DDL schema from file:
# mysql -u root -p < schema.sql
```

> Hibernate auto-creates tables on first deploy (`hbm2ddl.auto=update`). The admin account and demo data are seeded by `DatabaseInitializer` via `AppStartupListener.contextInitialized()`.

---

## Default Credentials (Auto-seeded on First Boot)

| Role | Email | Password |
|------|-------|----------|
| **ADMIN** | `admin@college.edu` | `AdminPassword123!` |
| **EMPLOYEE** | `shreyansh.singh@college.edu` | `EmpPassword123!` |
| **EMPLOYEE** | `jane.smith@college.edu` | `EmpPassword123!` |
| **EMPLOYEE** | `john.doe@college.edu` | `EmpPassword123!` |

Passwords are stored as **PBKDF2WithHmacSHA256 + random salt** — not plaintext.

---

## Build & Deploy Commands

```bash
# Build WAR (offline, using cached dependencies)
cd Shreyansh-Singh_SetB
JAVA_HOME=/opt/homebrew/opt/openjdk mvn -o clean package -DskipTests

# Copy WAR to Tomcat
cp target/EmployeeProjectAllocationSystem.war /path/to/apache-tomcat-10.1.x/webapps/

# Start Tomcat
/path/to/apache-tomcat-10.1.x/bin/startup.sh

# Visit application
open http://localhost:8080/EmployeeProjectAllocationSystem/login
```

---

## Key Design Decisions Mapped to Requirements

| Requirement | Implementation |
|---|---|
| **Bidirectional @ManyToMany** | `Employee` (owning, `@JoinTable`) / `Project` (inverse, `mappedBy`) with `employee_project` join table |
| **Session-based Auth** | `LoginServlet` creates `HttpSession`, stores `userId/userName/userRole`; `AuthenticationFilter` checks it |
| **Filter Chaining** | `RequestLoggingFilter → AuthenticationFilter → Servlet` in `web.xml` order; `chain.doFilter()` demonstrated |
| **`forward()` vs `sendRedirect()`** | `forward()` to ErrorServlet for 404/500; `sendRedirect()` for PRG after every form POST |
| **Post/Redirect/Get** | Every save/delete POST redirects to list with `?success=...` query param |
| **Multi-step Allocation** | Step 1 (form) → Step 2 (HttpSession stage) → Step 3 (review) → Step 4 (transactional confirm) |
| **Session Staging** | `session.setAttribute("pending_employee_id", ...)` during allocation; cleared after confirm or cancel |
| **RBAC** | `AuthenticationFilter` checks `session.getAttribute("userRole")` for admin-only paths |
| **Password Hashing** | `PasswordUtil.hashPassword()` uses `PBKDF2WithHmacSHA256` + 16-byte `SecureRandom` salt |
| **No Infinite Recursion** | `toString()`, `equals()`, `hashCode()` exclude the opposite collection in both entities |
| **Duplicate Prevention** | `Set<>` + `uk_employee_project` DB constraint + app-level check in `AllocationService` |

---

