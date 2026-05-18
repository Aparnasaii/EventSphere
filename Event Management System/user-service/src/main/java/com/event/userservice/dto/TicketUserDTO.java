package com.event.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TicketUserDTO {
    @JsonProperty("userId")
    private Long id;
    @JsonProperty("fullName")
    private String name;
    private String email;
    private String contactNumber;

    // Default Constructor
    public TicketUserDTO() {}

    // Parameterized Constructor
    public TicketUserDTO(Long id, String name, String email, String contactNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}