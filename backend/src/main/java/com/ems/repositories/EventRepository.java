package com.ems.repositories;

import com.ems.database.DatabaseConnection;
import com.ems.models.Event;
import com.ems.models.Event.EventStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    public boolean hasConflict(int venueId, Date eventDate, Time startTime, Time endTime, Integer excludeEventId) {
        String query = "SELECT COUNT(*) FROM Events WHERE venue_id = ? AND event_date = ? " +
                       "AND ((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?) OR (start_time >= ? AND end_time <= ?))";
        if (excludeEventId != null) {
            query += " AND event_id != ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, venueId);
            stmt.setDate(2, eventDate);
            stmt.setTime(3, endTime);
            stmt.setTime(4, startTime);
            stmt.setTime(5, endTime);
            stmt.setTime(6, startTime);
            stmt.setTime(7, startTime);
            stmt.setTime(8, endTime);

            if (excludeEventId != null) {
                stmt.setInt(9, excludeEventId);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // Default to conflict on error for safety
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM Events";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                events.add(mapRowToEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public Event getEventById(int id) {
        String query = "SELECT * FROM Events WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToEvent(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createEvent(Event event) {
        String query = "INSERT INTO Events (title, description, category_id, venue_id, image_url, event_date, start_time, end_time, capacity, created_by) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            setEventPreparedStatement(stmt, event);
            stmt.setInt(10, event.getCreatedBy());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateEvent(int id, Event event) {
        String query = "UPDATE Events SET title=?, description=?, category_id=?, venue_id=?, image_url=?, event_date=?, start_time=?, end_time=?, capacity=?, updated_by=? " +
                       "WHERE event_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            setEventPreparedStatement(stmt, event);
            if (event.getUpdatedBy() != null) {
                stmt.setInt(10, event.getUpdatedBy());
            } else {
                stmt.setNull(10, java.sql.Types.INTEGER);
            }
            stmt.setInt(11, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteEvent(int id) {
        String query = "DELETE FROM Events WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean endEvent(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("CALL endEvent(?)")) {
            stmt.setInt(1, id);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setEventPreparedStatement(PreparedStatement stmt, Event event) throws SQLException {
        stmt.setString(1, event.getTitle());
        stmt.setString(2, event.getDescription());
        stmt.setInt(3, event.getCategoryId());
        stmt.setInt(4, event.getVenueId());
        stmt.setString(5, event.getImageUrl());
        stmt.setDate(6, event.getEventDate());
        stmt.setTime(7, event.getStartTime());
        stmt.setTime(8, event.getEndTime());
        stmt.setInt(9, event.getCapacity());
    }

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getInt("event_id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.setCategoryId(rs.getInt("category_id"));
        event.setVenueId(rs.getInt("venue_id"));
        event.setImageUrl(rs.getString("image_url"));
        event.setEventDate(rs.getDate("event_date"));
        event.setStartTime(rs.getTime("start_time"));
        event.setEndTime(rs.getTime("end_time"));
        event.setCapacity(rs.getInt("capacity"));
        event.setCurrentRegistrations(rs.getInt("current_registrations"));
        event.setStatus(EventStatus.valueOf(rs.getString("status")));
        event.setCreatedBy(rs.getInt("created_by"));
        event.setCreatedAt(rs.getTimestamp("created_at"));
        return event;
    }
}
