package com.event.feedbackservice.controller;

import com.event.feedbackservice.dto.FeedbackRequestDTO;
import com.event.feedbackservice.dto.EventDTO;
import com.event.feedbackservice.model.Feedback;
import com.event.feedbackservice.service.FeedbackService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/initiate")
    public ResponseEntity<String> startFeedbackCycle(@Valid @RequestBody EventDTO event) {
        feedbackService.initiateFeedbackProcess(event);
        return ResponseEntity.ok("Feedback cycle initiated for event: " + event.getEventName());
    }

    @PutMapping("/submit/{feedbackId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ADMIN')")
    public ResponseEntity<String> submitUserFeedback(@PathVariable Long feedbackId,@Valid @RequestBody FeedbackRequestDTO feedbackrequest) {
        feedbackService.updateFeedback(feedbackId, feedbackrequest);
        return ResponseEntity.ok("Feedback submitted successfully.");
    }

    @GetMapping("/average/{eventId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ORGANIZER','ROLE_ADMIN')")
    public ResponseEntity<Double> getAverage(@PathVariable Long eventId) {
        return ResponseEntity.ok(feedbackService.getAverageRating(eventId));
    }

    @GetMapping("/comments/{eventId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ORGANIZER','ROLE_ADMIN')")
    public ResponseEntity<List<String>> getComments(@PathVariable Long eventId) {
        return ResponseEntity.ok(feedbackService.getAllComments(eventId));
    }

    @GetMapping("/event/{eventId}/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ORGANIZER','ROLE_ADMIN')")
    public ResponseEntity<Feedback> getFeedbackbyEventIdUserId(@PathVariable Long eventId, @PathVariable Long userId) {
        return ResponseEntity.ok(feedbackService.getUserFeedback(eventId, userId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ORGANIZER','ROLE_ADMIN')")
    public ResponseEntity<List<Feedback>> getFeedbacksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByUser(userId));
    }
}