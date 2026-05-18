package com.event.eventservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="ticket-service")
public interface TicketClient {
    @DeleteMapping("/tickets/event/{eventId}/cancel-tickets")
    void deletedTicketByEvent(@PathVariable Long eventId);
}
