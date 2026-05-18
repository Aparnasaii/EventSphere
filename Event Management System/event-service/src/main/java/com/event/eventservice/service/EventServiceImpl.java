package com.event.eventservice.service;

import com.event.eventservice.dto.*;
import com.event.eventservice.exceptions.EventNotFoundException;
import com.event.eventservice.exceptions.InsufficientSeatsException;
import com.event.eventservice.exceptions.SeatCapacityException;
import com.event.eventservice.models.Event;
import com.event.eventservice.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EventServiceImpl implements EventService {
	
	private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
	
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private NotificationClient notificationClient;
	@Autowired
	private TicketClient ticketClient;
	@Autowired
	private FeedbackClient feedbackClient;

    @Autowired
    private UserClient userClient;
	@Override
	public Event createEvent(Event event) {
		logger.info("Creating new event with name: {}", event.getName());
		if(event.getTotalCapacity()==null||event.getTotalCapacity()<=0) {
			logger.error("Invalid capacity for event: {}", event.getTotalCapacity());
			throw new RuntimeException("Capacity must be greater than 0");
		}
		event.setAvailableSeats(event.getTotalCapacity());
		event.setIsDeleted(false);
		// Normalize status to uppercase; default to ACTIVE if blank
		String status = event.getStatus();
		event.setStatus((status != null && !status.isBlank()) ? status.toUpperCase() : "ACTIVE");
		// Normalize category: map legacy "Tech" → "Technology", then Title Case
		if (event.getCategory() != null && !event.getCategory().isBlank()) {
			String cat = event.getCategory().trim();
			if (cat.equalsIgnoreCase("Tech")) cat = "Technology";
			event.setCategory(cat.substring(0, 1).toUpperCase() + cat.substring(1).toLowerCase());
		}
		event.setCreatedAt(LocalDateTime.now());
		event.setUpdatedAt(LocalDateTime.now());
		// Persist first — notification failure must never block event creation
		Event savedEvent = eventRepository.save(event);
		logger.info("Event created successfully with ID: {}", savedEvent.getEventId());
		try {
			BulkNotificationRequestDTO request = getBulkNotificationRequest(savedEvent, "EVENT_CREATED");
			notificationClient.sendBulk(request);
			logger.debug("Creation notification sent for event: {}", savedEvent.getName());
		} catch (Exception ex) {
			logger.warn("Could not send creation notification for event ID: {} — {}", savedEvent.getEventId(), ex.getMessage());
		}
		return savedEvent;
	}
	@Override
	public List<Event> getAllEvents(){
		logger.info("Fetching all active events");
		List<Event> events = eventRepository.findByIsDeletedFalse();
		logger.info("Retrieved {} active events", events.size());
		return events;
	}
	@Override
	public Event getEventById(Long id){
		logger.info("Fetching event with ID: {}", id);
		Event event = eventRepository.findById(id).filter(e->!e.getIsDeleted())
				.orElseThrow(()-> {
					logger.error("Event not found with ID: {}", id);
                    return new EventNotFoundException(id);
                });
		logger.info("Event found with ID: {}", id);
		return event;
	}
	@Override
	public Event updateEvent(Long id,Event updatedEvent) {
		logger.info("Updating event with ID: {}", id);
		Event existing= getEventById(id);
		existing.setName(updatedEvent.getName());
		existing.setCategory(updatedEvent.getCategory());
		existing.setLocation(updatedEvent.getLocation());
		existing.setEventDate(updatedEvent.getEventDate());
		existing.setStartTime(updatedEvent.getStartTime());
		existing.setEndTime(updatedEvent.getEndTime());
		existing.setDescription(updatedEvent.getDescription());
		existing.setTicketPrice(updatedEvent.getTicketPrice());
		existing.setUpdatedAt(LocalDateTime.now());
		if(updatedEvent.getTotalCapacity()!=null) {
			int diff=updatedEvent.getTotalCapacity()-existing.getTotalCapacity();
			logger.debug("Updating capacity for event ID: {}, capacity difference: {}", id, diff);
			existing.setTotalCapacity(updatedEvent.getTotalCapacity());
			existing.setAvailableSeats(Math.max(0, existing.getAvailableSeats()+diff));
		}
		// Persist FIRST — notification failure must never block event update
		Event savedEvent = eventRepository.save(existing);
		logger.info("Event with ID: {} updated successfully", id);
		try {
			BulkNotificationRequestDTO request = getBulkNotificationRequest(savedEvent, "EVENT_UPDATED");
			notificationClient.sendBulk(request);
			logger.debug("Update notification sent for event: {}", savedEvent.getName());
		} catch (Exception ex) {
			logger.warn("Could not send update notification for event ID: {} — {}", id, ex.getMessage());
		}
		return savedEvent;
	}
	@Override
	public void deleteEvent(Long id) {
		logger.info("Deleting event with ID: {}", id);
		Event event = getEventById(id);
		event.setIsDeleted(true);

		// Cancel associated tickets — non-blocking: log and continue if ticket-service is down
		try {
			logger.debug("Cancelling tickets associated with event ID: {}", id);
			ticketClient.deletedTicketByEvent(id);
			logger.debug("Tickets cancelled for event ID: {}", id);
		} catch (Exception ex) {
			logger.warn("Could not cancel tickets for event ID: {} — {}", id, ex.getMessage());
		}

		// Persist the soft-delete BEFORE attempting notifications
		eventRepository.save(event);
		logger.info("Event with ID: {} soft-deleted successfully", id);

		// Send notifications — non-blocking: log and continue if notification/user-service is down
		try {
			BulkNotificationRequestDTO request = getBulkNotificationRequest(event, "EVENT_DELETED");
			notificationClient.sendBulk(request);
			logger.debug("Deletion notification sent for event: {}", event.getName());
		} catch (Exception ex) {
			logger.warn("Could not send deletion notification for event ID: {} — {}", id, ex.getMessage());
		}
	}
	@Override
	@Transactional
	public void reduceSeats(Long id ,int count) {
		logger.info("Reducing seats for event ID: {} by count: {}", id, count);
		Event event=getEventById(id);
		if(event.getAvailableSeats()<count) {
			logger.error("Insufficient seats available. Available: {}, Requested: {}", event.getAvailableSeats(), count);
			throw new InsufficientSeatsException();
		}
		event.setAvailableSeats(event.getAvailableSeats()-count);
		eventRepository.save(event);
		logger.info("Seats reduced successfully. New available seats: {}", event.getAvailableSeats());
	}
	@Override
	@Transactional
	public void increaseSeats(Long id ,int count) {
		logger.info("Increasing seats for event ID: {} by count: {}", id, count);
		Event event=getEventById(id);
		if(event.getAvailableSeats()+count>event.getTotalCapacity()) {
			logger.error("Seat capacity exceeded. Available: {}, Current+Requested: {}, Max Capacity: {}", 
				event.getAvailableSeats(), event.getAvailableSeats()+count, event.getTotalCapacity());
			throw new SeatCapacityException();
		}
		event.setAvailableSeats(event.getAvailableSeats()+count);
		eventRepository.save(event);
		logger.info("Seats increased successfully. New available seats: {}", event.getAvailableSeats());
	}

	@Override
	public void triggerFeedback(Long id) {
		logger.info("Triggering feedback initiation for event ID: {}", id);
		Event event = getEventById(id);
		FeedbackDTO feedbackDTO = new FeedbackDTO(event.getEventId(), event.getName());
		feedbackClient.triggerFeedBack(feedbackDTO);
		logger.info("Feedback initiation request sent for event ID: {}", id);
	}
		
	@Override
	public List<Event> searchEvents(String category, String location, LocalDate date) {
		logger.info("Searching events with category: {}, location: {}, date: {}", category, location, date);
		List<Event> results;
		
	    // 1. All three filters provided
	    if (category != null && location != null && date != null) {
	        results = eventRepository.findByCategoryAndLocationAndEventDateAndIsDeletedFalse(category, location, date);
	    } 
	    // 2. Category and Location
	    else if (category != null && location != null) {
	        results = eventRepository.findByCategoryAndLocationAndIsDeletedFalse(category, location);
	    } 
	    
	    // 3. Category and Date
	    else if (category != null && date != null) {
	        results = eventRepository.findByCategoryAndEventDateAndIsDeletedFalse(category, date);
	    } 
	    
	    // 4. Location and Date
	    else if (location != null && date != null) {
	        results = eventRepository.findByLocationAndEventDateAndIsDeletedFalse(location, date);
	    } 
	    
	    // 5. Only Category
	    else if (category != null) {
	        results = eventRepository.findByCategoryAndIsDeletedFalse(category);
	    } 
	    
	    // 6. Only Location
	    else if (location != null) {
	        results = eventRepository.findByLocationAndIsDeletedFalse(location);
	    } 
	    
	    // 7. Only Date
	    else if (date != null) {
	        results = eventRepository.findByEventDateAndIsDeletedFalse(date);
	    } 
	    
	    // 8. No filters provided - return all active events
	    else {
	        results = eventRepository.findByIsDeletedFalse();
	    }
	    
	    logger.info("Search completed. Found {} events", results.size());
	    return results;
	}
		@Override
    public List<Event> getEventsByCategory(String category) {
        logger.info("Fetching events by category: {}", category);
        List<Event> events = eventRepository.findByCategoryAndIsDeletedFalse(category);
        logger.info("Retrieved {} events for category: {}", events.size(), category);
        return events;
    }

    @Override
    public List<Event> getEventsByLocation(String location) {
        logger.info("Fetching events by location: {}", location);
        List<Event> events = eventRepository.findByLocationAndIsDeletedFalse(location);
        logger.info("Retrieved {} events for location: {}", events.size(), location);
        return events;
    }

    @Override
    public List<Event> getEventsByOrganizer(Long organizerId) {
        logger.info("Fetching events by organizer ID: {}", organizerId);
        List<Event> events = eventRepository.findByOrganizerIdAndIsDeletedFalse(organizerId);
        logger.info("Retrieved {} events for organizer ID: {}", events.size(), organizerId);
        return events;
    }

    @Override
    public List<Event> getEventsByTimeRange(LocalDateTime start, LocalDateTime end) {
        logger.info("Fetching events by time range from: {} to: {}", start, end);
        List<Event> events = eventRepository.findByStartTimeBetweenAndIsDeletedFalse(start, end);
        logger.info("Retrieved {} events for time range", events.size());
        return events;
    }

    public BulkNotificationRequestDTO getBulkNotificationRequest(Event event,String templateName) {
        List<RecipientDetails> recipientDetails = userClient.getRecipients();
        BulkNotificationRequestDTO requestDTO = BulkNotificationRequestDTO.builder()
                .templateName(templateName)
                .type(NotificationType.EMAIL)
                .commonData(Map.of("eventName",event.getName()))
                .recipients(recipientDetails)
                .build();
        return requestDTO;

    }
}
