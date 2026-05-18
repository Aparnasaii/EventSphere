package com.event.eventservice.service;

import com.event.eventservice.dto.BulkNotificationRequestDTO;
import com.event.eventservice.dto.NotificationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.event.eventservice.dto.NotificationRequestDTO;


@FeignClient("notification-service")
public interface NotificationClient {
	@PostMapping("/api/v1/notifications/send")
    NotificationResponseDTO sendNotification(@RequestBody  NotificationRequestDTO notificationRequest);

    @PostMapping("/api/v1/notifications/bulk")
    String sendBulk(@RequestBody BulkNotificationRequestDTO bulkNotificationRequestDTO);


}
