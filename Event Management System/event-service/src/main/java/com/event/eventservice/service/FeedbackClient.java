package com.event.eventservice.service;

import com.event.eventservice.dto.FeedbackDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="feedback-service")
public interface FeedbackClient {
	@PostMapping("/feedback/initiate")
	void triggerFeedBack(@RequestBody FeedbackDTO feedbackDTO);
}
