package com.ems.services;

import com.ems.models.Venue;
import com.ems.repositories.VenueRepository;

import java.util.List;

public class VenueService {
    private VenueRepository venueRepository;

    public VenueService() {
        this.venueRepository = new VenueRepository();
    }

    public List<Venue> getAllVenues() {
        return venueRepository.getAllVenues();
    }

    public Venue getVenueById(int id) {
        return venueRepository.getVenueById(id);
    }

    public boolean createVenue(Venue venue) {
        if (venue.getCapacity() <= 0) return false;
        return venueRepository.createVenue(venue);
    }

    public boolean updateVenue(int id, Venue venue) {
        if (venue.getCapacity() <= 0) return false;
        return venueRepository.updateVenue(id, venue);
    }

    public boolean deleteVenue(int id) {
        return venueRepository.deleteVenue(id);
    }
}
