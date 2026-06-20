package com.ems.repositories;

import com.ems.database.DatabaseConnection;
import com.ems.models.Venue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VenueRepository {

    public List<Venue> getAllVenues() {
        List<Venue> venues = new ArrayList<>();
        String query = "SELECT * FROM Venues";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                venues.add(mapRowToVenue(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }

    public Venue getVenueById(int id) {
        String query = "SELECT * FROM Venues WHERE venue_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToVenue(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createVenue(Venue venue) {
        String query = "INSERT INTO Venues (venue_name, location, capacity, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, venue.getVenueName());
            stmt.setString(2, venue.getLocation());
            stmt.setInt(3, venue.getCapacity());
            stmt.setString(4, venue.getDescription());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateVenue(int id, Venue venue) {
        String query = "UPDATE Venues SET venue_name=?, location=?, capacity=?, description=? WHERE venue_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, venue.getVenueName());
            stmt.setString(2, venue.getLocation());
            stmt.setInt(3, venue.getCapacity());
            stmt.setString(4, venue.getDescription());
            stmt.setInt(5, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteVenue(int id) {
        String query = "DELETE FROM Venues WHERE venue_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Venue mapRowToVenue(ResultSet rs) throws SQLException {
        Venue venue = new Venue();
        venue.setVenueId(rs.getInt("venue_id"));
        venue.setVenueName(rs.getString("venue_name"));
        venue.setLocation(rs.getString("location"));
        venue.setCapacity(rs.getInt("capacity"));
        venue.setDescription(rs.getString("description"));
        return venue;
    }
}
