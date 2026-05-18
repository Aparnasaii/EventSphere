package com.event.feedbackservice.dto;

public class NotificationRequestDTO {
    private Long userId;
    private String email;
    private String message;

    public NotificationRequestDTO() {}
    public NotificationRequestDTO(Long userId, String email, String message) {
        this.userId = userId;
        this.email = email;
        this.message = message;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}