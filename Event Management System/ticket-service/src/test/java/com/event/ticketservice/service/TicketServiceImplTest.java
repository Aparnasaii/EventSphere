package com.event.ticketservice.service;

import com.event.ticketservice.client.UserClient;
import com.event.ticketservice.dto.FeedbackUserDTO;
import com.event.ticketservice.dto.UserDTO;
import com.event.ticketservice.entity.Ticket;
import com.event.ticketservice.entity.TicketStatus;
import com.event.ticketservice.repository.TicketRepository;
//import com.event.ticketservice.service.TicketServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private TicketServiceImpl ticketService;

    public TicketServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUsersForEvent_onlyConfirmedTicketsReturned() {
        // Arrange: create dummy tickets
        Ticket confirmedTicket = new Ticket();
        confirmedTicket.setTicketId(1L);
        confirmedTicket.setUserId(101L);
        confirmedTicket.setEventId(55L);
        confirmedTicket.setStatus(TicketStatus.CONFIRMED);
        confirmedTicket.setBookingDate(LocalDateTime.now());

        Ticket cancelledTicket = new Ticket();
        cancelledTicket.setTicketId(2L);
        cancelledTicket.setUserId(102L);
        cancelledTicket.setEventId(55L);
        cancelledTicket.setStatus(TicketStatus.CANCELLED);

        when(ticketRepository.findByEventId(55L))
                .thenReturn(Arrays.asList(confirmedTicket, cancelledTicket));

        UserDTO user = new UserDTO();
        user.setId(101L);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setContactNumber("9876543210");

        when(userClient.getUser(101L)).thenReturn(user);

        // Act
        List<FeedbackUserDTO> result = ticketService.getUsersForEvent(55L);

        // Assert
        assertEquals(1, result.size()); // only confirmed ticket should be included
        assertEquals(101L, result.get(0).getUserId());
        assertEquals("alice@example.com", result.get(0).getUserEmail());

        // Verify interactions
        verify(ticketRepository, times(1)).findByEventId(55L);
        verify(userClient, times(1)).getUser(101L);
        verify(userClient, never()).getUser(102L); // cancelled ticket ignored
    }
}
