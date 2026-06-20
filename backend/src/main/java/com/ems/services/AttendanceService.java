package com.ems.services;

import com.ems.models.Attendance;
import com.ems.repositories.AttendanceRepository;

import java.util.List;

import java.util.Map;

public class AttendanceService {
    private AttendanceRepository attendanceRepository;

    public AttendanceService() {
        this.attendanceRepository = new AttendanceRepository();
    }

    public boolean markAttendance(int eventId, int participantId, String status) {
        return attendanceRepository.markAttendance(eventId, participantId, status);
    }

    public List<Attendance> getAttendanceForEvent(int eventId) {
        return attendanceRepository.getAttendanceForEvent(eventId);
    }

    public List<Map<String, Object>> getEventParticipantsWithAttendance(int eventId) {
        return attendanceRepository.getEventParticipantsWithAttendance(eventId);
    }
}
