package com.ems.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/ems_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root"; // Defaulting to root. Should be an env variable in production.

    public static Connection getConnection() {
        try {
            // Ensure driver is loaded
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
            return null;
        }
    }
}
