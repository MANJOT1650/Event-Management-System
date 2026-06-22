package com.ems.database;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() {
        try {
            // Ensure driver is loaded
            Class.forName("org.postgresql.Driver");
            
            String databaseUrl = System.getenv("DATABASE_URL");
            
            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                // If it's a Render internal/external URL (postgres://)
                if (databaseUrl.startsWith("postgres://")) {
                    URI dbUri = new URI(databaseUrl);
                    String username = dbUri.getUserInfo().split(":")[0];
                    String password = dbUri.getUserInfo().split(":")[1];
                    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
                    return DriverManager.getConnection(dbUrl, username, password);
                } else {
                    // Assume it's already a valid JDBC URL
                    return DriverManager.getConnection(databaseUrl);
                }
            } else {
                // Local fallback
                String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
                String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
                String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "ems_db";
                String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
                String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
                String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "root";
                
                return DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException | ClassNotFoundException | URISyntaxException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
            return null;
        }
    }
}
