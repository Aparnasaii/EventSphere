package com.event.ticketservice.service;

import com.event.ticketservice.dto.FeedbackUserDTO;
import com.event.ticketservice.entity.Ticket;

import java.util.List;
import com.event.ticketservice.dto.TicketBookingResponseDTO;
public interface TicketService {

    void cancelAllTicketsByEventId(Long eventId);

    TicketBookingResponseDTO bookTicket(Ticket ticket);

    void cancelTicket(Long ticketId);

    //Ticket getTicketById(Long ticketId);

    List<Ticket> getTicketsByUserId(Long userId);

    List<Ticket> getTicketsByEventId(Long eventId);

	List<FeedbackUserDTO> getUsersForEvent(Long eventId);

	void confirmTicket(Long ticketId);

}
