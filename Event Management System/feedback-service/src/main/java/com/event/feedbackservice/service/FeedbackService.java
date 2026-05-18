package com.event.feedbackservice.service;

import com.event.feedbackservice.dto.EventDTO;
import com.event.feedbackservice.dto.FeedbackRequestDTO;
import com.event.feedbackservice.model.Feedback;
import java.util.List;

public interface FeedbackService {

    void initiateFeedbackProcess(EventDTO event);

    void updateFeedback(Long feedbackId, FeedbackRequestDTO feedbackrequest);

    Double getAverageRating(Long eventId);

    List<String> getAllComments(Long eventId);

    Feedback getUserFeedback(Long eventId, Long userId);

    List<Feedback> getFeedbacksByUser(Long userId);
}