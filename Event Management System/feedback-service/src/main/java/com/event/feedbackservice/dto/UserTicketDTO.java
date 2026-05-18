package com.event.feedbackservice.dto;

import lombok.Data;

@Data
public class UserTicketDTO {

    private Long userId;

    private String userEmail;

    public UserTicketDTO() {}
    public UserTicketDTO(Long userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;
    }
}