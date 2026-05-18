package com.event.eventservice.service;


import com.event.eventservice.dto.RecipientDetails;
import com.event.eventservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
        // Define methods to call user-service endpoints
    @GetMapping("/api/users/all")
    List<UserDTO> getAllUsers();

    @GetMapping("/api/users/recipients")
    List<RecipientDetails> getRecipients();
}
