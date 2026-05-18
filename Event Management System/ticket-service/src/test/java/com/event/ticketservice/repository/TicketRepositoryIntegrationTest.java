package com.event.ticketservice.repository;

import com.event.ticketservice.entity.Ticket;
import com.event.ticketservice.entity.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TicketRepositoryIntegrationTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void testSaveAndFindByUserId() {
        Ticket ticket = new Ticket();
        ticket.setUserId(101L);
        ticket.setEventId(55L);
        ticket.setQuantity(2);
        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.setPrice(1000.0);

        ticketRepository.save(ticket);

        List<Ticket> tickets = ticketRepository.findByUserId(101L);
        assertEquals(1, tickets.size());
        assertEquals(55L, tickets.get(0).getEventId());
    }

    @Test
    void testFindByEventId() {
        Ticket ticket1 = new Ticket();
        ticket1.setUserId(101L);
        ticket1.setEventId(55L);
        ticket1.setQuantity(2);
        ticket1.setStatus(TicketStatus.CONFIRMED);
        ticket1.setPrice(1000.0);

        Ticket ticket2 = new Ticket();
        ticket2.setUserId(102L);
        ticket2.setEventId(55L);
        ticket2.setQuantity(1);
        ticket2.setStatus(TicketStatus.CONFIRMED);
        ticket2.setPrice(500.0);

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        List<Ticket> tickets = ticketRepository.findByEventId(55L);
        assertEquals(2, tickets.size());
    }
}
