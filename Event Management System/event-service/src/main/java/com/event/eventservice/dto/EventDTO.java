package com.event.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private String name;
	private Long eventId;
	private Double ticketPrice;
	private Integer totalCapacity;
	private Integer availableSeats;
}
