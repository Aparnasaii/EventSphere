package com.event.ticketservice.controller;

import com.event.ticketservice.dto.FeedbackUserDTO;
import com.event.ticketservice.dto.TicketBookingResponseDTO;
import com.event.ticketservice.entity.Ticket;
import com.event.ticketservice.entity.TicketStatus;
import com.event.ticketservice.repository.TicketRepository;
import com.event.ticketservice.service.TicketService;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketService      ticketService;
    private final TicketRepository   ticketRepository;

    public TicketController(TicketService ticketService, TicketRepository ticketRepository) {
        this.ticketService    = ticketService;
        this.ticketRepository = ticketRepository;
    }

    // --- Book Ticket ---
    @PostMapping("/book")
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ADMIN')")
    public TicketBookingResponseDTO bookTicket(@RequestBody Ticket ticket) {
        log.info("Received ticket booking request for userId={} and eventId={}", ticket.getUserId(), ticket.getEventId());
        TicketBookingResponseDTO response = ticketService.bookTicket(ticket);
        log.info("Ticket booking processed successfully");
        return response;
    }

    // --- Cancel Ticket ---
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ADMIN')")
    @PostMapping("/cancel/{ticketId}")
    public void cancelTicket(@PathVariable Long ticketId, HttpServletRequest request) {
        log.info("Received ticket cancellation request for ticketId={}", ticketId);

        // Ownership check: attendees may only cancel their own tickets
        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader   = request.getHeader("X-User-Role");
        if (userIdHeader != null && !"ROLE_ADMIN".equals(roleHeader)) {
            Long requestingUserId = Long.parseLong(userIdHeader);
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
            if (!ticket.getUserId().equals(requestingUserId)) {
                log.warn("User {} attempted to cancel ticket {} owned by user {}",
                        requestingUserId, ticketId, ticket.getUserId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own tickets");
            }
        }

        ticketService.cancelTicket(ticketId);
        log.info("Ticket cancellation processed for ticketId={}", ticketId);
    }

    // --- Stripe Checkout Success ---
    @GetMapping("/payment/success")
    public void paymentSuccess(@RequestParam("session_id") String sessionId,
                               HttpServletResponse response) throws Exception {
        log.info("Received payment success callback for sessionId={}", sessionId);
        try {
            Session session = Session.retrieve(sessionId);

            String ticketIdStr = session.getMetadata().get("ticketId");
            if (ticketIdStr != null) {
                Long ticketId = Long.valueOf(ticketIdStr);
                ticketService.confirmTicket(ticketId);
                log.info("Ticket confirmed after successful payment for ticketId={}", ticketId);
            } else {
                log.warn("No ticketId metadata found for sessionId={}", sessionId);
            }

            // Redirect the browser tab back to the Angular frontend's My Tickets page
            response.sendRedirect("http://localhost:4200/tickets?payment=success");

        } catch (Exception e) {
            log.error("Error handling payment success callback for sessionId={}", sessionId, e);
            // On error redirect to frontend with failure flag
            response.sendRedirect("http://localhost:4200/tickets?payment=failed");
        }
    }

    // --- Stripe Checkout Cancel ---
    @GetMapping("/payment/cancel")
    public void paymentCancel(HttpServletResponse response) throws Exception {
        log.info("Payment was cancelled by user");
        // Redirect back to Angular events list with cancellation notice
        response.sendRedirect("http://localhost:4200/events?payment=cancelled");
    }

//    // --- Get Ticket by ID ---
//    @GetMapping("/{ticketId}")
//    public Ticket getTicketById(@PathVariable Long ticketId) {
//        return ticketService.getTicketById(ticketId);
//    }


    // --- Get Tickets by User ID ---
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ATTENDEE','ROLE_ORGANIZER','ROLE_ADMIN')")
    public List<Ticket> getTicketsByUserId(@PathVariable Long userId) {
        log.info("Fetching tickets for userId={}", userId);
        return ticketService.getTicketsByUserId(userId);
    }

    // --- Get Tickets by Event ID ---
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER','ROLE_ADMIN')")
    public List<Ticket> getTicketsByEventId(@PathVariable Long eventId) {
        log.info("Fetching tickets for eventId={}", eventId);
        return ticketService.getTicketsByEventId(eventId);
    }

    // --- Get Users for Feedback Service by Event ID ---
    @GetMapping("/feedback/event/{eventId}")
    public List<FeedbackUserDTO> getUsersForFeedback(@PathVariable Long eventId) {
        log.info("Fetching feedback users for eventId={}", eventId);
        return ticketService.getUsersForEvent(eventId);
    }
    @DeleteMapping("/event/{eventId}/cancel-tickets")
    public ResponseEntity<Void> cancelAllTicketsByEvent(@PathVariable Long eventId) {
        ticketService.cancelAllTicketsByEventId(eventId);
        return ResponseEntity.noContent().build();
    }


}
