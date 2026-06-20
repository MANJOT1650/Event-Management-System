package com.ems.repositories;

import com.ems.database.DatabaseConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class RegistrationRepository {

    public boolean registerParticipant(int eventId, int participantId) {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("CALL registerParticipant(?, ?)")) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, participantId);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelRegistration(int eventId, int participantId) {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("CALL cancelRegistration(?, ?)")) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, participantId);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
