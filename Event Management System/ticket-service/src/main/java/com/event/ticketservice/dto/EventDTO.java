package com.event.ticketservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventDTO {
    private Long    eventId;
    private String  name;
    private Double  ticketPrice;
    private Integer totalCapacity;
    private Integer availableSeats;

    // --- Getters and Setters ---
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }

    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
}
