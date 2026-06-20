package com.ems.services;

import com.ems.models.Event;
import com.ems.repositories.EventRepository;

import java.util.List;

public class EventService {
    private EventRepository eventRepository;

    public EventService() {
        this.eventRepository = new EventRepository();
    }

    public List<Event> getAllEvents() {
        return eventRepository.getAllEvents();
    }

    public Event getEventById(int id) {
        return eventRepository.getEventById(id);
    }

    public boolean createEvent(Event event) {
        if (eventRepository.hasConflict(event.getVenueId(), event.getEventDate(), event.getStartTime(), event.getEndTime(), null)) {
            return false; // Conflict found
        }
        return eventRepository.createEvent(event);
    }

    public boolean updateEvent(int id, Event event) {
        if (eventRepository.hasConflict(event.getVenueId(), event.getEventDate(), event.getStartTime(), event.getEndTime(), id)) {
            return false; // Conflict found
        }
        return eventRepository.updateEvent(id, event);
    }

    public boolean deleteEvent(int id) {
        return eventRepository.deleteEvent(id);
    }

    public boolean endEvent(int id) {
        return eventRepository.endEvent(id);
    }
}
