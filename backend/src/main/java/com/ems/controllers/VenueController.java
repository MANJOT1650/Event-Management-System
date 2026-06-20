package com.ems.controllers;

import com.ems.models.Venue;
import com.ems.services.VenueService;
import io.javalin.http.Context;

public class VenueController {
    private VenueService venueService;

    public VenueController() {
        this.venueService = new VenueService();
    }

    public void getAllVenues(Context ctx) {
        ctx.json(venueService.getAllVenues());
    }

    public void getVenueById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Venue venue = venueService.getVenueById(id);
        if (venue != null) {
            ctx.json(venue);
        } else {
            ctx.status(404).json("{\"message\": \"Venue not found\"}");
        }
    }

    public void createVenue(Context ctx) {
        Venue venue = ctx.bodyAsClass(Venue.class);
        if (venueService.createVenue(venue)) {
            ctx.status(201).json("{\"message\": \"Venue created successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Invalid venue data\"}");
        }
    }

    public void updateVenue(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Venue venue = ctx.bodyAsClass(Venue.class);
        if (venueService.updateVenue(id, venue)) {
            ctx.json("{\"message\": \"Venue updated successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to update venue\"}");
        }
    }

    public void deleteVenue(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (venueService.deleteVenue(id)) {
            ctx.json("{\"message\": \"Venue deleted successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to delete venue\"}");
        }
    }
}
