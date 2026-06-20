package com.ems.controllers;

import com.ems.models.Event;
import com.ems.services.EventService;
import io.javalin.http.Context;

public class EventController {
    private EventService eventService;

    public EventController() {
        this.eventService = new EventService();
    }

    public void getAllEvents(Context ctx) {
        ctx.json(eventService.getAllEvents());
    }

    public void getEventById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Event event = eventService.getEventById(id);
        if (event != null) {
            ctx.json(event);
        } else {
            ctx.status(404).json("{\"message\": \"Event not found\"}");
        }
    }

    public void createEvent(Context ctx) {
        Event event = ctx.bodyAsClass(Event.class);
        Integer userId = ctx.attribute("userId");
        if (userId != null) event.setCreatedBy(userId);

        if (eventService.createEvent(event)) {
            ctx.status(201).json("{\"message\": \"Event created successfully\"}");
        } else {
            ctx.status(409).json("{\"message\": \"Conflict detected or invalid data\"}");
        }
    }

    public void updateEvent(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Event event = ctx.bodyAsClass(Event.class);
        Integer userId = ctx.attribute("userId");
        if (userId != null) event.setUpdatedBy(userId);

        if (eventService.updateEvent(id, event)) {
            ctx.json("{\"message\": \"Event updated successfully\"}");
        } else {
            ctx.status(409).json("{\"message\": \"Conflict detected or failed to update\"}");
        }
    }

    public void deleteEvent(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (eventService.deleteEvent(id)) {
            ctx.json("{\"message\": \"Event deleted successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to delete event\"}");
        }
    }

    public void endEvent(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (eventService.endEvent(id)) {
            ctx.json("{\"message\": \"Event ended successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to end event\"}");
        }
    }
}
