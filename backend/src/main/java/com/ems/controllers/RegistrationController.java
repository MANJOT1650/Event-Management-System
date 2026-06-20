package com.ems.controllers;

import com.ems.services.RegistrationService;
import io.javalin.http.Context;

public class RegistrationController {
    private RegistrationService registrationService;

    public RegistrationController() {
        this.registrationService = new RegistrationService();
    }

    public void registerParticipant(Context ctx) {
        int eventId = Integer.parseInt(ctx.pathParam("eventId"));
        Integer participantId = ctx.attribute("userId"); // From JWT

        if (participantId == null) {
            ctx.status(401).json("{\"message\": \"Unauthorized\"}");
            return;
        }

        if (registrationService.registerParticipant(eventId, participantId)) {
            ctx.json("{\"message\": \"Registration request processed (check waitlist if full)\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to register (maybe already registered)\"}");
        }
    }

    public void cancelRegistration(Context ctx) {
        int eventId = Integer.parseInt(ctx.pathParam("eventId"));
        Integer participantId = ctx.attribute("userId");

        if (participantId == null) {
            ctx.status(401).json("{\"message\": \"Unauthorized\"}");
            return;
        }

        if (registrationService.cancelRegistration(eventId, participantId)) {
            ctx.json("{\"message\": \"Registration cancelled successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to cancel registration\"}");
        }
    }
}
