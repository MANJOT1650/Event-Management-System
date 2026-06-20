package com.ems.repositories;

import com.ems.database.DatabaseConnection;
import com.ems.models.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardRepository {

    public List<AuditLog> getAuditLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT a.*, u.name AS user_name FROM AuditLogs a LEFT JOIN Users u ON a.user_id = u.user_id ORDER BY a.timestamp DESC LIMIT 50";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setLogId(rs.getInt("log_id"));
                log.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
                log.setUserName(rs.getString("user_name"));
                log.setAction(rs.getString("action"));
                log.setTableName(rs.getString("table_name"));
                log.setRecordId(rs.getObject("record_id") != null ? rs.getInt("record_id") : null);
                log.setTimestamp(rs.getTimestamp("timestamp"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public List<Map<String, Object>> getEventStatistics() {
        List<Map<String, Object>> stats = new ArrayList<>();
        String query = "SELECT * FROM EventStatistics_View";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("eventId", rs.getInt("event_id"));
                stat.put("title", rs.getString("title"));
                stat.put("capacity", rs.getInt("capacity"));
                stat.put("currentRegistrations", rs.getInt("current_registrations"));
                stat.put("availableSeats", rs.getInt("available_seats"));
                stat.put("occupancyPercentage", rs.getDouble("occupancy_percentage"));
                stats.add(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public List<Map<String, Object>> getParticipantHistory(int participantId) {
        List<Map<String, Object>> history = new ArrayList<>();
        String query = "SELECT * FROM ParticipantHistory_View WHERE participant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, participantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("eventId", rs.getInt("event_id"));
                record.put("title", rs.getString("title"));
                record.put("eventDate", rs.getDate("event_date"));
                record.put("eventStatus", rs.getString("event_status"));
                record.put("registeredAt", rs.getTimestamp("registered_at"));
                record.put("attendanceStatus", rs.getString("attendance_status"));
                history.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}
