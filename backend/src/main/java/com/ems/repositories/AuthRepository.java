package com.ems.repositories;

import com.ems.database.DatabaseConnection;
import com.ems.models.Role;
import com.ems.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthRepository {

    public User getUserByEmail(String email) {
        String query = "SELECT u.user_id, u.name, u.email, u.password_hash, u.created_at, r.role_name " +
                       "FROM Users u JOIN Roles r ON u.role_id = r.role_id WHERE u.email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setRole(Role.valueOf(rs.getString("role_name")));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createUser(User user) {
        String roleQuery = "SELECT role_id FROM Roles WHERE role_name = ?";
        String insertQuery = "INSERT INTO Users (name, email, password_hash, role_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement roleStmt = conn.prepareStatement(roleQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            
            // Get role ID
            roleStmt.setString(1, user.getRole().name());
            ResultSet rs = roleStmt.executeQuery();
            if (rs.next()) {
                int roleId = rs.getInt("role_id");
                
                insertStmt.setString(1, user.getName());
                insertStmt.setString(2, user.getEmail());
                insertStmt.setString(3, user.getPasswordHash());
                insertStmt.setInt(4, roleId);
                
                int rows = insertStmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
