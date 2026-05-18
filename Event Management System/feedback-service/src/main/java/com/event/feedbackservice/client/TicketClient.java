package com.event.feedbackservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.event.feedbackservice.dto.UserTicketDTO;

import java.util.List;

@FeignClient(name = "ticket-service")
public interface TicketClient {
    // Now returns a List of objects containing ID and Email
    @GetMapping("/tickets/feedback/event/{eventId}")
    List<UserTicketDTO> getAttendeesByEvent(@PathVariable Long eventId);


}