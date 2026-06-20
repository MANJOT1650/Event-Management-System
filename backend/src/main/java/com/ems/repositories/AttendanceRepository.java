package com.ems.repositories;

import com.ems.database.DatabaseConnection;
import com.ems.models.Attendance;
import com.ems.models.Attendance.AttendanceStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRepository {

    public boolean markAttendance(int eventId, int participantId, String status) {
        String query = "INSERT INTO Attendance (event_id, participant_id, status) VALUES (?, ?, ?) " +
                       "ON CONFLICT (event_id, participant_id) DO UPDATE SET status = ?, marked_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, participantId);
            stmt.setString(3, status);
            stmt.setString(4, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Attendance> getAttendanceForEvent(int eventId) {
        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM Attendance WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Attendance att = new Attendance();
                att.setAttendanceId(rs.getInt("attendance_id"));
                att.setEventId(rs.getInt("event_id"));
                att.setParticipantId(rs.getInt("participant_id"));
                att.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
                att.setMarkedAt(rs.getTimestamp("marked_at"));
                list.add(att);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, Object>> getEventParticipantsWithAttendance(int eventId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "SELECT u.user_id, u.name, u.email, a.status " +
                       "FROM Registrations r " +
                       "JOIN Users u ON r.participant_id = u.user_id " +
                       "LEFT JOIN Attendance a ON r.event_id = a.event_id AND r.participant_id = a.participant_id " +
                       "WHERE r.event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", rs.getInt("user_id"));
                map.put("name", rs.getString("name"));
                map.put("email", rs.getString("email"));
                map.put("attendanceStatus", rs.getString("status") != null ? rs.getString("status") : "PENDING");
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
