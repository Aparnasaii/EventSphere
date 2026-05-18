package com.event.eventservice.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.event.eventservice.dto.EventDTO;
import com.event.eventservice.models.Event;
import com.event.eventservice.service.EventService;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventService eventService;

    //Basic CRUD

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER','ROLE_ADMIN')")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        logger.info("Received request to create event: {}", event);
        Event createdEvent = eventService.createEvent(event);
        logger.info("Event created successfully with ID: {}", createdEvent.getEventId());
        return ResponseEntity.ok(createdEvent);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        logger.info("Received request to fetch all events");
        List<Event> events = eventService.getAllEvents();
        logger.info("Retrieved {} events", events.size());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        logger.info("Received request to fetch event with ID: {}", id);
        Event event = eventService.getEventById(id);
        logger.info("Event with ID {} retrieved successfully", id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER','ROLE_ADMIN')")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody Event event) {
        logger.info("Received request to update event with ID: {}", id);
        Event updatedEvent = eventService.updateEvent(id, event);
        logger.info("Event with ID {} updated successfully", id);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER','ROLE_ADMIN')")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        logger.info("Received request to delete event with ID: {}", id);
        eventService.deleteEvent(id);
        logger.info("Event with ID {} deleted successfully", id);
        return ResponseEntity.ok("Event deleted successfully");
    }
    @GetMapping("/{id}/ticket-info")
    public EventDTO getEventTicketInfo(@PathVariable Long id) {
        logger.info("Received request to fetch ticket info for event ID: {}", id);
    	Event event = eventService.getEventById(id);
    	logger.info("Ticket info retrieved for event ID: {}", id);
	    return new EventDTO(event.getName(), event.getEventId(), event.getTicketPrice(), event.getTotalCapacity(), event.getAvailableSeats());
    }
    //  Seat Management

    @PutMapping("/{id}/reduce-seats")
    public ResponseEntity<Void> reduceSeats(@PathVariable Long id, @RequestParam int count) {
        logger.info("Received request to reduce seats for event ID: {} by count: {}", id, count);
        eventService.reduceSeats(id, count);
        logger.info("Seats reduced successfully for event ID: {}", id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/increase-seats")
    public ResponseEntity<Void> increaseSeats(@PathVariable Long id, @RequestParam int count) {
        logger.info("Received request to increase seats for event ID: {} by count: {}", id, count);
        eventService.increaseSeats(id, count);
        logger.info("Seats increased successfully for event ID: {}", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ORGANIZER','ROLE_ADMIN')")
    public ResponseEntity<String> triggerFeedback(@PathVariable Long id) {
        logger.info("Received request to trigger feedback for event ID: {}", id);
        eventService.triggerFeedback(id);
        logger.info("Feedback trigger completed for event ID: {}", id);
        return ResponseEntity.ok("Feedback initiated successfully");
    }

    //  Search and Filters
    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("Received request to search events with category: {}, location: {}, date: {}", category, location, date);
        List<Event> events = eventService.searchEvents(category, location, date);
        logger.info("Search returned {} events", events.size());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Event>> getEventsByCategory(@PathVariable String category) {
        logger.info("Received request to fetch events by category: {}", category);
        List<Event> events = eventService.getEventsByCategory(category);
        logger.info("Retrieved {} events for category: {}", events.size(), category);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<Event>> getEventsByLocation(@PathVariable String location) {
        logger.info("Received request to fetch events by location: {}", location);
        List<Event> events = eventService.getEventsByLocation(location);
        logger.info("Retrieved {} events for location: {}", events.size(), location);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<Event>> getEventsByOrganizer(@PathVariable Long organizerId) {
        logger.info("Received request to fetch events by organizer ID: {}", organizerId);
        List<Event> events = eventService.getEventsByOrganizer(organizerId);
        logger.info("Retrieved {} events for organizer ID: {}", events.size(), organizerId);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/range")
    public ResponseEntity<List<Event>> getEventsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        logger.info("Received request to fetch events by time range from: {} to: {}", start, end);
        List<Event> events = eventService.getEventsByTimeRange(start, end);
        logger.info("Retrieved {} events for time range", events.size());
        return ResponseEntity.ok(events);
    }
}