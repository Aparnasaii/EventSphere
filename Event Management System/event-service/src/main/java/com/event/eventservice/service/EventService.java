package com.event.eventservice.service;

import com.event.eventservice.models.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
	Event createEvent(Event event);
	List<Event> getAllEvents();
	Event getEventById(Long id);
	Event updateEvent(Long id,Event event);
	void deleteEvent(Long id);
	void reduceSeats(Long id, int count);
	void increaseSeats(Long id, int count);
	void triggerFeedback(Long id);
	List<Event> getEventsByCategory(String category);
    List<Event> getEventsByLocation(String location);
    List<Event> getEventsByOrganizer(Long organizerId);
    List<Event> getEventsByTimeRange(LocalDateTime start, LocalDateTime end);
	List<Event> searchEvents(String category, String location, LocalDate date);
}
