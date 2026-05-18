package com.event.eventservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.event.eventservice.models.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event,Long> {
	// Normal Search
	List<Event> findByCategoryAndIsDeletedFalse(String category);
	List<Event> findByLocationAndIsDeletedFalse(String location);
	List<Event> findByEventDateAndIsDeletedFalse(LocalDate eventDate);
	List<Event> findByIsDeletedFalse();
	//Combined Search
	List<Event> findByCategoryAndLocationAndIsDeletedFalse(String category,String location);
	//Active Events
	List<Event> findByStatusAndIsDeletedFalse(String status);
	//Events Created By organizer
	List<Event> findByOrganizerId(Long OrganizerId);
	List<Event> findByOrganizerIdAndIsDeletedFalse(Long userId);
	List<Event> findByOrganizerIdAndIsDeletedTrue(Long userId);
	List<Event> findByEndTimeBeforeAndIsDeletedFalse(LocalDateTime time);
	List<Event> findByStartTimeBetweenAndIsDeletedFalse(LocalDateTime from,LocalDateTime to);
	Boolean existsByEventIdAndIsDeletedFalse(Long eventId);
	List<Event> findByCategoryAndLocationAndEventDateAndIsDeletedFalse(String category, String location,
			LocalDate date);
	List<Event> findByCategoryAndEventDateAndIsDeletedFalse(String category, LocalDate date);
	List<Event> findByLocationAndEventDateAndIsDeletedFalse(String location, LocalDate date);
}
