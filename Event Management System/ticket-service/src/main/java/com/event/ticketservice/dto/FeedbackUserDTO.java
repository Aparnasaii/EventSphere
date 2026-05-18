package com.event.ticketservice.dto;

public class FeedbackUserDTO {
    private Long userId;
    private String userEmail;
    public FeedbackUserDTO(Long userId,String userEmail) {
        this.userId = userId;
        this.userEmail = String.valueOf(userEmail);
    }
    public FeedbackUserDTO(){

    }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
