package com.ems;

import com.ems.controllers.AttendanceController;
import com.ems.controllers.AuthController;
import com.ems.controllers.CategoryController;
import com.ems.controllers.DashboardController;
import com.ems.controllers.EventController;
import com.ems.controllers.NotificationController;
import com.ems.controllers.RegistrationController;
import com.ems.controllers.UserController;
import com.ems.controllers.VenueController;
import com.ems.middleware.AuthMiddleware;
import com.ems.middleware.AuthMiddleware.AppRole;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {
    public static void main(String[] args) {
        AuthController authController = new AuthController();
        VenueController venueController = new VenueController();
        CategoryController categoryController = new CategoryController();
        EventController eventController = new EventController();
        RegistrationController registrationController = new RegistrationController();
        AttendanceController attendanceController = new AttendanceController();
        NotificationController notificationController = new NotificationController();
        DashboardController dashboardController = new DashboardController();
        UserController userController = new UserController();
        try (java.sql.Connection conn = com.ems.database.DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("ALTER TABLE Events ADD COLUMN updated_by INT REFERENCES Users(user_id) ON DELETE SET NULL");
                System.out.println("Column updated_by added.");
            } catch (Exception e) {}
            String functionSql = 
                "CREATE OR REPLACE FUNCTION audit_events() RETURNS TRIGGER AS $$\n" +
                "BEGIN\n" +
                "    IF TG_OP = 'INSERT' THEN\n" +
                "        INSERT INTO AuditLogs (user_id, action, table_name, record_id) VALUES (NEW.created_by, 'CREATE_EVENT', 'Events', NEW.event_id);\n" +
                "    ELSIF TG_OP = 'UPDATE' THEN\n" +
                "        INSERT INTO AuditLogs (user_id, action, table_name, record_id) VALUES (COALESCE(NEW.updated_by, NEW.created_by), 'UPDATE_EVENT', 'Events', NEW.event_id);\n" +
                "    ELSIF TG_OP = 'DELETE' THEN\n" +
                "        INSERT INTO AuditLogs (user_id, action, table_name, record_id) VALUES (OLD.created_by, 'DELETE_EVENT', 'Events', OLD.event_id);\n" +
                "    END IF;\n" +
                "    RETURN NULL;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql;";
            stmt.execute(functionSql);
            System.out.println("Trigger patched!");
        } catch (Exception e) { e.printStackTrace(); }

        // Start Javalin app
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            try {
                config.staticFiles.add("../frontend", Location.EXTERNAL); // Serve frontend static files locally
            } catch (Exception e) {
                System.out.println("Frontend directory not found, serving API only.");
            }
            config.router.apiBuilder(() -> {
                path("/api", () -> {
                    path("/auth", () -> {
                        post("/login", authController::login, AppRole.ANYONE);
                        post("/register", authController::register, AppRole.ANYONE);
                    });
                    
                    path("/venues", () -> {
                        get(venueController::getAllVenues, AppRole.ANYONE);
                        get("/{id}", venueController::getVenueById, AppRole.ANYONE);
                        post(venueController::createVenue, AppRole.ADMIN, AppRole.COORDINATOR);
                        put("/{id}", venueController::updateVenue, AppRole.ADMIN, AppRole.COORDINATOR);
                        delete("/{id}", venueController::deleteVenue, AppRole.ADMIN, AppRole.COORDINATOR);
                    });

                    path("/categories", () -> {
                        get(categoryController::getAllCategories, AppRole.ANYONE);
                        get("/{id}", categoryController::getCategoryById, AppRole.ANYONE);
                        post(categoryController::createCategory, AppRole.ADMIN, AppRole.COORDINATOR);
                        delete("/{id}", categoryController::deleteCategory, AppRole.ADMIN, AppRole.COORDINATOR);
                    });

                    path("/events", () -> {
                        get(eventController::getAllEvents, AppRole.ANYONE);
                        get("/{id}", eventController::getEventById, AppRole.ANYONE);
                        post(eventController::createEvent, AppRole.ADMIN, AppRole.COORDINATOR);
                        put("/{id}", eventController::updateEvent, AppRole.ADMIN, AppRole.COORDINATOR);
                        delete("/{id}", eventController::deleteEvent, AppRole.ADMIN);
                        post("/{id}/end", eventController::endEvent, AppRole.ADMIN, AppRole.COORDINATOR);
                        
                        post("/{eventId}/register", registrationController::registerParticipant, AppRole.PARTICIPANT);
                        delete("/{eventId}/cancel", registrationController::cancelRegistration, AppRole.PARTICIPANT);

                        post("/{eventId}/attendance/{participantId}", attendanceController::markAttendance, AppRole.ADMIN, AppRole.COORDINATOR);
                        get("/{eventId}/attendance", attendanceController::getAttendance, AppRole.ADMIN, AppRole.COORDINATOR);
                        get("/{eventId}/participants", attendanceController::getEventParticipants, AppRole.ADMIN, AppRole.COORDINATOR);
                    });

                    path("/notifications", () -> {
                        get(notificationController::getUserNotifications, AppRole.ADMIN, AppRole.COORDINATOR, AppRole.PARTICIPANT);
                        put("/{id}/read", notificationController::markAsRead, AppRole.ADMIN, AppRole.COORDINATOR, AppRole.PARTICIPANT);
                    });

                    path("/dashboard", () -> {
                        get("/audit-logs", dashboardController::getAuditLogs, AppRole.ADMIN);
                        get("/statistics", dashboardController::getEventStatistics, AppRole.ADMIN);
                        get("/history", dashboardController::getParticipantHistory, AppRole.PARTICIPANT);
                    });

                    path("/users", () -> {
                        get(userController::getAllUsers, AppRole.ADMIN);
                        delete("/{id}", userController::deleteUser, AppRole.ADMIN);
                        put("/{id}/role", userController::updateUserRole, AppRole.ADMIN);
                        put("/profile", userController::updateProfile, AppRole.ADMIN, AppRole.COORDINATOR, AppRole.PARTICIPANT);
                    });
                });
            });
        });
        
        app.beforeMatched(AuthMiddleware::manageAccess);
        
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json("{\"message\": \"Internal Server Error: " + e.getMessage() + "\"}");
        });

        app.start(7070);

        System.out.println("EMS Backend Server started on http://localhost:7070");
    }
}
