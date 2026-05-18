package com.event.feedbackservice.service;

import com.event.feedbackservice.client.NotificationClient;
import com.event.feedbackservice.client.TicketClient;
import com.event.feedbackservice.dto.BulkNotificationRequestDTO;
import com.event.feedbackservice.dto.EventDTO;
import com.event.feedbackservice.dto.FeedbackRequestDTO;
import com.event.feedbackservice.dto.NotificationType;
import com.event.feedbackservice.dto.RecipientDetails;
import com.event.feedbackservice.dto.UserTicketDTO;
import com.event.feedbackservice.exception.ResourceNotFoundException;
import com.event.feedbackservice.model.Feedback;
import com.event.feedbackservice.model.enums.FeedbackStatus;
import com.event.feedbackservice.repository.FeedbackRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final TicketClient        ticketClient;
    private final NotificationClient  notificationClient;

    // ── Initiate feedback cycle after event ends ─────────────────
    @Override
    @Transactional
    public void initiateFeedbackProcess(EventDTO event) {
        log.info("Initiating feedback process for eventId={}, eventName={}",
                 event.getEventId(), event.getEventName());

        // 1. Fetch all confirmed attendees from ticket-service
        List<UserTicketDTO> attendees;
        try {
            attendees = ticketClient.getAttendeesByEvent(event.getEventId());
        } catch (Exception e) {
            log.error("Could not fetch attendees for eventId={}: {}", event.getEventId(), e.getMessage());
            return;
        }

        if (attendees == null || attendees.isEmpty()) {
            log.warn("No attendees found for eventId={}. Skipping feedback initiation.", event.getEventId());
            return;
        }

        // 2. Create a PENDING Feedback record for each attendee (skip duplicates)
        int created = 0;
        for (UserTicketDTO attendee : attendees) {
            Optional<Feedback> existing =
                feedbackRepository.findByEventIdAndUserId(event.getEventId(), attendee.getUserId());

            if (existing.isEmpty()) {
                Feedback fb = new Feedback();
                fb.setEventId(event.getEventId());
                fb.setEventName(event.getEventName());
                fb.setUserId(attendee.getUserId());
                fb.setEmail(attendee.getUserEmail());
                fb.setStatus(FeedbackStatus.PENDING);
                feedbackRepository.save(fb);
                created++;
            }
        }
        log.info("Created {} new PENDING feedback records for eventId={}", created, event.getEventId());

        // 3. Send bulk notification (non-blocking — failure must not abort the process)
        try {
            List<RecipientDetails> recipients = attendees.stream()
                .map(a -> RecipientDetails.builder()
                        .userId(a.getUserId())
                        .email(a.getUserEmail())
                        .build())
                .collect(Collectors.toList());

            BulkNotificationRequestDTO notification = BulkNotificationRequestDTO.builder()
                .templateName("FEEDBACK_REQUEST")
                .type(NotificationType.EMAIL)
                .commonData(Map.of("eventName", event.getEventName()))
                .recipients(recipients)
                .build();

            notificationClient.notifyUserToSubmitFeedback(notification);
            log.info("Feedback notification sent to {} attendees for eventId={}", recipients.size(), event.getEventId());
        } catch (Exception e) {
            log.warn("Could not send feedback notifications for eventId={}: {}", event.getEventId(), e.getMessage());
        }
    }

    // ── User submits / updates their rating and comment ──────────
    @Override
    @Transactional
    public void updateFeedback(Long feedbackId, FeedbackRequestDTO req) {
        log.info("Updating feedback id={} with rating={}", feedbackId, req.getRating());

        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Feedback record not found with id: " + feedbackId));

        feedback.setRating(req.getRating());
        feedback.setComment(req.getComment());
        feedback.setStatus(FeedbackStatus.COMPLETED);
        feedback.setSubmittedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        log.info("Feedback id={} marked COMPLETED with rating={}", feedbackId, req.getRating());
    }

    // ── Average rating for an event ──────────────────────────────
    @Override
    public Double getAverageRating(Long eventId) {
        Double avg = feedbackRepository.getAverageRatingByEventId(eventId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;   // round to 1 dp
    }

    // ── All submitted comments for an event ──────────────────────
    @Override
    public List<String> getAllComments(Long eventId) {
        return feedbackRepository.findAllCommentsByEventId(eventId, FeedbackStatus.COMPLETED)
               .stream()
               .filter(c -> c != null && !c.isBlank())
               .collect(Collectors.toList());
    }

    // ── Specific user's feedback record for an event ─────────────
    @Override
    public Feedback getUserFeedback(Long eventId, Long userId) {
        return feedbackRepository.findByEventIdAndUserId(eventId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No feedback record found for userId=" + userId + " and eventId=" + eventId));
    }

    // ── All feedback records for a user (across all events) ──────
    @Override
    public List<Feedback> getFeedbacksByUser(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }
}
