package com.event.feedbackservice.client;

import com.event.feedbackservice.dto.BulkNotificationRequestDTO;
import com.event.feedbackservice.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/api/v1/notifications/bulk")
    void notifyUserToSubmitFeedback(@RequestBody BulkNotificationRequestDTO request);
}