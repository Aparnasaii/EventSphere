package com.event.userservice.dto;

import com.event.userservice.entity.Role;

public class EventUserDTO {
    private Long id;
    private Role role;

    public EventUserDTO() {}

    public EventUserDTO(Long id, Role role) {
        this.id = id;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}