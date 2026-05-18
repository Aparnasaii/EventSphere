package com.event.ticketservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {
    @JsonProperty("userId")
    private Long id;
    @JsonProperty("fullName")
    private String name;
    @JsonProperty("email")
    private String email;

    private String contactNumber; // Changed from phoneNum to match User entity
 
    // Default Constructor

    public UserDTO() {}
 
    // Parameterized Constructor (Updated field name)

    public UserDTO(Long id, String name, String email, String contactNumber) {

        this.id = id;

        this.name = name;

        this.email = email;

        this.contactNumber = contactNumber;

    }
 
    // Getters and Setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }
 
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
 
    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }
 
    public String getContactNumber() { return contactNumber; }

    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

}
 