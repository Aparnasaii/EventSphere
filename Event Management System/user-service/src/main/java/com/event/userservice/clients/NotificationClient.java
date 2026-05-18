package com.event.userservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Note: Ensure you import the DTOs from your user-service package structure
import com.event.userservice.dto.NotificationRequestDTO;
import com.event.userservice.dto.NotificationResponseDTO;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications/send")
    ResponseEntity<NotificationResponseDTO> sendNotification(@RequestBody NotificationRequestDTO request);

}