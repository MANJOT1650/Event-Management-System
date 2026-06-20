package com.ems.controllers;

import com.ems.services.AttendanceService;
import io.javalin.http.Context;

public class AttendanceController {
    private AttendanceService attendanceService;

    public AttendanceController() {
        this.attendanceService = new AttendanceService();
    }

    public void markAttendance(Context ctx) {
        int eventId = Integer.parseInt(ctx.pathParam("eventId"));
        int participantId = Integer.parseInt(ctx.pathParam("participantId"));
        // Expected body: {"status": "PRESENT" or "ABSENT"}
        String status = ctx.bodyAsClass(StatusRequest.class).status;

        if (attendanceService.markAttendance(eventId, participantId, status)) {
            ctx.json("{\"message\": \"Attendance marked successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to mark attendance\"}");
        }
    }

    public void getAttendance(Context ctx) {
        int eventId = Integer.parseInt(ctx.pathParam("eventId"));
        ctx.json(attendanceService.getAttendanceForEvent(eventId));
    }

    public void getEventParticipants(Context ctx) {
        int eventId = Integer.parseInt(ctx.pathParam("eventId"));
        ctx.json(attendanceService.getEventParticipantsWithAttendance(eventId));
    }

    private static class StatusRequest {
        public String status;
    }
}
