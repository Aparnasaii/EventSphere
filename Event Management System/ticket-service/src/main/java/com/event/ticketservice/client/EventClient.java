package com.event.ticketservice.client;

import com.event.ticketservice.dto.EventDTO;
import org.springframework.stereotype.Component;
import java.util.*;





import com.event.ticketservice.dto.EventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/events/{eventId}/ticket-info")
    EventDTO getEvent(@PathVariable("eventId") Long eventId);

    @PutMapping("/events/{eventId}/reduce-seats")
    void reserveSeats(@PathVariable("eventId") Long eventId,
                      @RequestParam("count") int count);

    @PutMapping("/events/{eventId}/increase-seats")
    void releaseSeats(@PathVariable("eventId") Long eventId,
                      @RequestParam("count") int count);
}
