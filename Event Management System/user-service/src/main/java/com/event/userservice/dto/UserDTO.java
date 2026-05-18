package com.event.userservice.dto;

import com.event.userservice.entity.Role;
import com.event.userservice.entity.UserStatus;

public class UserDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String contactNumber;
    private Role role;
    private UserStatus status;

    // 1. Default Constructor (Required for JSON)
    public UserDTO() {}

    // 2. Parameterized Constructor
    public UserDTO(Long userId, String fullName, String email, String contactNumber, Role role, UserStatus status) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.role = role;
        this.status = status;
    }

    // 3. Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "UserDTO [userId=" + userId + ", fullName=" + fullName + ", email=" + email + ", contactNumber=" + contactNumber + ", role=" + role + "]";
    }
}