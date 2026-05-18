package com.event.ticketservice.dto;

public class TicketBookingResponseDTO {
    private Long ticketId;
    private String status;
    private String checkoutUrl;

    public TicketBookingResponseDTO(Long ticketId, String status, String checkoutUrl) {
        this.ticketId = ticketId;
        this.status = status;
        this.checkoutUrl = checkoutUrl;
    }

    // Getters and setters
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
}
