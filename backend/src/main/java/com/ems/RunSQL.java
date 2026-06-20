package com.ems;

import com.ems.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class RunSQL {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Add updated_by column
            try {
                stmt.execute("ALTER TABLE Events ADD COLUMN updated_by INT REFERENCES Users(user_id) ON DELETE SET NULL");
                System.out.println("Column updated_by added.");
            } catch (Exception e) {
                System.out.println("Column might already exist: " + e.getMessage());
            }

            // Update the audit trigger function
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
            System.out.println("Trigger function updated.");

            // Seed Categories
            String seedCategories = "INSERT INTO Categories (category_name) VALUES " +
                "('Workshop'), ('Conference'), ('Seminar'), ('Networking'), " +
                "('Hackathon'), ('Meetup'), ('Concert'), ('Sports'), " +
                "('Exhibition'), ('Webinar') " +
                "ON CONFLICT (category_name) DO NOTHING;";
            stmt.execute(seedCategories);
            System.out.println("Categories seeded.");

            // Seed Venues
            String[] venues = {
                "INSERT INTO Venues (venue_name, location, capacity, description) SELECT 'Tech Hub', 'Downtown', 200, 'Modern tech space' WHERE NOT EXISTS (SELECT 1 FROM Venues WHERE venue_name = 'Tech Hub');",
                "INSERT INTO Venues (venue_name, location, capacity, description) SELECT 'Convention Center', 'City Square', 1000, 'Large space for conferences' WHERE NOT EXISTS (SELECT 1 FROM Venues WHERE venue_name = 'Convention Center');",
                "INSERT INTO Venues (venue_name, location, capacity, description) SELECT 'Outdoor Arena', 'Riverside', 5000, 'Open air venue' WHERE NOT EXISTS (SELECT 1 FROM Venues WHERE venue_name = 'Outdoor Arena');",
                "INSERT INTO Venues (venue_name, location, capacity, description) SELECT 'Innovation Lab', 'Campus', 50, 'Small lab for workshops' WHERE NOT EXISTS (SELECT 1 FROM Venues WHERE venue_name = 'Innovation Lab');",
                "INSERT INTO Venues (venue_name, location, capacity, description) SELECT 'Virtual', 'Online', 10000, 'Online webinar space' WHERE NOT EXISTS (SELECT 1 FROM Venues WHERE venue_name = 'Virtual');",
                "INSERT INTO Venues (venue_name, location, capacity, description) SELECT 'Grand Hotel', 'West End', 500, 'Luxury conference rooms' WHERE NOT EXISTS (SELECT 1 FROM Venues WHERE venue_name = 'Grand Hotel');"
            };
            for (String v : venues) {
                stmt.execute(v);
            }
            System.out.println("Venues seeded.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
