package com.ems.repositories;

import com.ems.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ems.models.User;
import com.ems.models.Role;

public class UserRepository {

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT u.user_id, u.name, u.email, u.created_at, r.role_name " +
                       "FROM Users u JOIN Roles r ON u.role_id = r.role_id ORDER BY u.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setRole(Role.valueOf(rs.getString("role_name")));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean deleteUser(int userId) {
        String query = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserRole(int userId, Role newRole) {
        String query = "UPDATE Users SET role_id = (SELECT role_id FROM Roles WHERE role_name = ?) WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newRole.name());
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserProfile(int userId, String name, String email, String hashedPassword) {
        String query;
        if (hashedPassword != null && !hashedPassword.trim().isEmpty()) {
            query = "UPDATE Users SET name = ?, email = ?, password_hash = ? WHERE user_id = ?";
        } else {
            query = "UPDATE Users SET name = ?, email = ? WHERE user_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, name);
            stmt.setString(2, email);
            
            if (hashedPassword != null && !hashedPassword.trim().isEmpty()) {
                stmt.setString(3, hashedPassword);
                stmt.setInt(4, userId);
            } else {
                stmt.setInt(3, userId);
            }
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
