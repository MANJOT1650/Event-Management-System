# Event Management System (EMS)

A comprehensive, full-stack Event Management System built with Java, Javalin, PostgreSQL, and modern Vanilla HTML/CSS/JS. This platform supports distinct roles (Participant, Coordinator, Admin) and features advanced database mechanisms for high consistency and automated workflow management.

## 🏗 Architecture

The project is structured into two main components:
- **Backend**: A RESTful API built in Java using the highly performant [Javalin](https://javalin.io/) framework. It utilizes raw JDBC for direct, optimized database communication without ORM overhead.
- **Frontend**: A sleek, responsive, and minimalist web interface built with vanilla HTML, CSS, and JavaScript. It features dynamic rendering, dark mode, and token-based authentication.

## ✨ Features

- **Role-Based Access Control (RBAC)**: Distinct dashboards and permissions for `ADMIN`, `COORDINATOR`, and `PARTICIPANT`.
- **Event Lifecycle Management**: Create, update, view, and end events.
- **Automated Waitlist**: Seamless and automated waitlisting system handled entirely at the database layer.
- **Attendance Tracking**: Dynamic attendance management (Present/Absent) via the Coordinator dashboard.
- **Monochrome Minimalist UI**: State-of-the-art UI with responsive design, pill-shaped inputs, and automatic dark-mode support.

---

## 🗄️ Advanced Database Management System (ADBMS)

This project heavily leverages advanced PostgreSQL features to ensure data integrity, atomicity, and to offload complex business logic directly to the database layer. 

### 1. Stored Procedures (Automated Workflows)
We use Stored Procedures to handle complex, multi-step transactions atomically.

* **`registerParticipant(p_event_id, p_participant_id)`**
  * Uses row-level locking (`FOR UPDATE`) to prevent race conditions during concurrent registrations.
  * Checks event capacity in real-time. If seats are available, inserts the user into `Registrations`.
  * If the event is full, automatically routes the user to the `Waitlist` table.
  * Dispatches an automated entry into the `Notifications` table.

* **`cancelRegistration(p_event_id, p_participant_id)`**
  * Deletes a user from the `Registrations` table.
  * Automatically scans the `Waitlist` for the earliest queued participant (`ORDER BY joined_at ASC LIMIT 1`).
  * If a waitlisted user exists, they are automatically **promoted** to a full Registration, and an automated notification is dispatched to alert them.

### 2. Triggers (Data Integrity & Auditing)
Triggers are utilized to maintain denormalized statistical data and provide immutable audit trails.

* **Registration Counters**
  * `trg_after_registration_insert`: Automatically increments the `current_registrations` counter on the `Events` table whenever a new row is added to `Registrations`.
  * `trg_after_registration_delete`: Automatically decrements the counter when a registration is cancelled.
* **Audit Logging**
  * `trg_audit_events`: Listens for `INSERT`, `UPDATE`, and `DELETE` operations on the `Events` table and automatically injects a record into the `AuditLogs` table tracking the action, the table name, the record ID, and the `user_id` of the actor.

### 3. Views (Complex Data Abstraction)
We use Views to securely expose aggregated data without complex joins in the application layer.

* **`LiveEvents_View`**: Pre-joins `Events`, `Categories`, and `Venues` exclusively for events where `status = 'LIVE'`.
* **`ParticipantHistory_View`**: An aggregated view combining `Registrations`, `Events`, and `Attendance` to provide a complete historical snapshot of a user's activity.
* **`EventStatistics_View`**: Computes mathematical derivatives on the fly, including `available_seats` and `occupancy_percentage`.

### 4. Indexing (Performance Optimization)
Strategic B-Tree indexes are applied to frequently queried columns to ensure rapid lookups at scale:
* `idx_events_title`
* `idx_events_date`
* `idx_users_email`
* `idx_registrations_participant`

---

## 🚀 Running the Project

### Prerequisites
- Java 17+
- Maven
- PostgreSQL

### Setup
1. Create a PostgreSQL database named `ems`.
2. Update your credentials in `backend/src/main/java/com/ems/database/DatabaseConnection.java`.
3. Run the schema creation script found in `backend/database/schema.sql` to initialize the ADBMS logic.
4. Execute `./start.bat` in the root directory to compile the backend and start the Javalin server.
5. The application will be accessible at `http://localhost:7070/index.html`.
