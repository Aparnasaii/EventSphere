package com.event.ticketservice.dto;

import com.event.ticketservice.entity.TicketStatus;
import java.time.LocalDateTime;

public class TicketNotificationDTO {
    private Long ticketId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userContactNumber;
    private Long eventId;
    private int quantity;
    private double price;
    private LocalDateTime bookingDate;
    private TicketStatus status;
	public Long getTicketId() {
		return ticketId;
	}
	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	
	public String getUserContactNumber() {
		return userContactNumber;
	}
	public void setUserContactNumber(String userContactNumber) {
		this.userContactNumber = userContactNumber;
	}
	public Long getEventId() {
		return eventId;
	}
	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public LocalDateTime getBookingDate() {
		return bookingDate;
	}
	public void setBookingDate(LocalDateTime bookingDate) {
		this.bookingDate = bookingDate;
	}
	public TicketStatus getStatus() {
		return status;
	}
	public void setStatus(TicketStatus status) {
		this.status = status;
	}
    
    
}
