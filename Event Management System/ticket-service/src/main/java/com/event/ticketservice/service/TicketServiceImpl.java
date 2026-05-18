package com.event.ticketservice.service;

import com.event.ticketservice.client.EventClient;
import com.event.ticketservice.client.NotificationClient;
import com.event.ticketservice.client.UserClient;
import com.event.ticketservice.dto.*;
import com.event.ticketservice.entity.Ticket;
import com.event.ticketservice.entity.TicketStatus;
import com.event.ticketservice.enums.NotificationType;
import com.event.ticketservice.repository.TicketRepository;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final EventClient eventClient;
    private final NotificationClient notificationClient;
    private final UserClient userClient;
    private final StripePaymentUtil stripePaymentUtil;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             EventClient eventClient,
                             NotificationClient notificationClient,
                             UserClient userClient,
                             StripePaymentUtil stripePaymentUtil) {
        this.ticketRepository = ticketRepository;
        this.eventClient = eventClient;
        this.notificationClient = notificationClient;
        this.userClient = userClient;
        this.stripePaymentUtil = stripePaymentUtil;
    }
    @Override
    @Transactional
    public void cancelAllTicketsByEventId(Long eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);

        if (tickets.isEmpty()) {
            return;
        }

        for (Ticket ticket : tickets) {
            ticket.setStatus(TicketStatus.CANCELLED);
            Ticket saved = ticketRepository.save(ticket);
            UserDTO user = userClient.getUser(ticket.getUserId());
            log.info("User DTO received from User Service: {}", userSummary(user));
            notificationClient.sendNotification(buildNotificationRequestDTO(saved,user));
        }


    }
    @Override
    public TicketBookingResponseDTO bookTicket(Ticket ticket) {
        log.info("Book ticket request received for userId={} and eventId={}", ticket.getUserId(), ticket.getEventId());

        // Default quantity to 1 if not provided (defensive null-safety)
        if (ticket.getQuantity() == null || ticket.getQuantity() < 1) {
            ticket.setQuantity(1);
        }

        // Validate user
        UserDTO user = userClient.getUser(ticket.getUserId());
        log.info("User DTO received from User Service: {}", userSummary(user));
        if (user == null) {
            log.warn("User not found for userId={}", ticket.getUserId());
            throw new RuntimeException("User not found");
        }

        // Validate event and seats
        EventDTO event = eventClient.getEvent(ticket.getEventId());
        log.info("Event DTO received from Event Service: {}", eventSummary(event));
        if (event.getAvailableSeats() < ticket.getQuantity()) {
            log.warn("Not enough seats for eventId={}, requested={}, available={}",
                    ticket.getEventId(), ticket.getQuantity(), event.getAvailableSeats());
            throw new RuntimeException("Not enough seats available");
        }

        // Reserve seats in Event Service
        eventClient.reserveSeats(ticket.getEventId(), ticket.getQuantity());

        // Calculate price
        double totalPrice = event.getTicketPrice() * ticket.getQuantity();
        ticket.setPrice(totalPrice);

        if (totalPrice == 0.0) {
            // Free event → confirm immediately
            ticket.setStatus(TicketStatus.CONFIRMED);
            Ticket saved = ticketRepository.save(ticket);
            log.info("Free ticket booked and confirmed for ticketId={}", saved.getTicketId());

            // Notify Notification Service
            notificationClient.sendNotification(buildNotificationRequestDTO(saved,user));

            return new TicketBookingResponseDTO(
                saved.getTicketId(),
                saved.getStatus().toString(),
                null // no payment URL for free events
            );
        } else {
            // Paid event → go through Stripe
            ticket.setStatus(TicketStatus.PENDING_PAYMENT);
            Ticket saved = ticketRepository.save(ticket);
            log.info("Ticket saved with pending payment for ticketId={}", saved.getTicketId());

            try {
                Session session = stripePaymentUtil.createCheckoutSession(
                    saved.getTicketId(),
                    saved.getPrice(),
                    "inr",
                    "http://localhost:8084/tickets/payment/success",
                    "http://localhost:8084/tickets/payment/cancel"
                );
                log.info("Stripe checkout session created for ticketId={}", saved.getTicketId());

                return new TicketBookingResponseDTO(
                    saved.getTicketId(),
                    saved.getStatus().toString(),
                    session.getUrl()
                );

            } catch (Exception e) {
                saved.setStatus(TicketStatus.FAILED);
                ticketRepository.save(saved);

                // Release seats if payment session creation fails
                eventClient.releaseSeats(ticket.getEventId(), ticket.getQuantity());

                // Notify Notification Service about failure
                notificationClient.sendNotification(buildNotificationRequestDTO(saved,user));

                log.error("Payment flow failed for ticketId={}", saved.getTicketId(), e);

                throw new RuntimeException("Payment failed: " + e.getMessage());
            }
        }
    }



    @Override
    public void cancelTicket(Long ticketId) {
        log.info("Cancel ticket request received for ticketId={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket saved = ticketRepository.save(ticket);

        // Release seats in Event Service
        eventClient.releaseSeats(ticket.getEventId(), ticket.getQuantity());

        // Notify Notification Service
        UserDTO user = userClient.getUser(ticket.getUserId());
        log.info("User DTO received from User Service: {}", userSummary(user));
        notificationClient.sendNotification(buildNotificationRequestDTO(saved,user));
        log.info("Ticket cancelled for ticketId={}", ticketId);
    }

    @Override
    public void confirmTicket(Long ticketId) {
        log.info("Confirm ticket request received for ticketId={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(TicketStatus.CONFIRMED);
        Ticket saved = ticketRepository.save(ticket);

        // Optionally release seats or send notifications
        UserDTO user = userClient.getUser(ticket.getUserId());
        log.info("User DTO received from User Service: {}", userSummary(user));
        notificationClient.sendNotification(buildNotificationRequestDTO(saved, user));
        log.info("Ticket confirmed for ticketId={}", ticketId);
    }

    @Override
    public List<Ticket> getTicketsByUserId(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    @Override
    public List<Ticket> getTicketsByEventId(Long eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    @Override
    public List<FeedbackUserDTO> getUsersForEvent(Long eventId) {
        log.info("Fetching feedback users for eventId={}", eventId);
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);

        List<Long> userIds = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.CONFIRMED)
                .map(Ticket::getUserId)
                .distinct()
                .collect(Collectors.toList());

        log.info("Fetching user DTOs in batch from User Service for userIds={}", userIds);
        List<UserDTO> feedbackUsers = userClient.getUserBatch(userIds);
        log.info("User DTO batch received from User Service: {}", feedbackUsers.stream()
                .map(this::userSummary)
                .collect(Collectors.toList()));

        return feedbackUsers.stream()
                .map(user -> new FeedbackUserDTO(user.getId(),user.getEmail()))
                .collect(Collectors.toList());
    }


    private NotificationRequestDTO buildNotificationRequestDTO(Ticket ticket, UserDTO user) {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setUserId(ticket.getUserId());
        request.setRecipient(user.getEmail());
        // Choose the template that matches the actual ticket status
        String templateName;
        switch (ticket.getStatus()) {
            case CONFIRMED:        templateName = "TICKET_CONFIRMED";  break;
            case CANCELLED:        templateName = "TICKET_CANCELLED";  break;
            case FAILED:           templateName = "TICKET_FAILED";     break;
            case PENDING_PAYMENT:  templateName = "TICKET_PENDING";    break;
            default:               templateName = "TICKET_CONFIRMED";  break;
        }
        request.setTemplateName(templateName);
        request.setType(NotificationType.EMAIL);

        Map<String, String> templateData = new HashMap<>();
        templateData.put("userName", user.getName());
        templateData.put("eventId", String.valueOf(ticket.getEventId()));
        templateData.put("quantity", String.valueOf(ticket.getQuantity()));
        EventDTO event = eventClient.getEvent(ticket.getEventId());
        log.info("Event DTO received from Event Service: {}", eventSummary(event));
        templateData.put("eventName", event.getName());
        templateData.put("price", String.format("%.2f", ticket.getPrice()));
        templateData.put("ticketId", String.valueOf(ticket.getTicketId()));
        templateData.put("seatInfo","VIP_A_102");
        request.setTemplateData(templateData);

        return request;
    }

    private String userSummary(UserDTO user) {
        if (user == null) {
            return "null";
        }
        return String.format("id=%s, name=%s, email=%s, contactNumber=%s",
                user.getId(), user.getName(), user.getEmail(), user.getContactNumber());
    }

    private String eventSummary(EventDTO event) {
        if (event == null) {
            return "null";
        }
        return String.format("eventId=%s, name=%s, ticketPrice=%s, availableSeats=%s",
                event.getEventId(), event.getName(), event.getTicketPrice(), event.getAvailableSeats());
    }

}
