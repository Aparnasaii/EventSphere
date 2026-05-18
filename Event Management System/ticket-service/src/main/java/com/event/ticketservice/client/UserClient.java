package com.event.ticketservice.client;

import com.event.ticketservice.dto.UserDTO;
import org.springframework.stereotype.Component;

//@Component
//public class UserClient {
//
//    public UserDTO getUser(Long userId) {
//        // Return a dummy user for testing
//        return new UserDTO(userId, "Test User", "testuser@example.com", "9999999999");
//    }
//}
//





import com.event.ticketservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    /** Uses the permit-all ticket-context endpoint — no JWT needed for Feign calls */
    @GetMapping("/api/users/{id}/ticket-context")
    UserDTO getUser(@PathVariable("id") Long id);

    @PostMapping("/api/users/batch")
    List<UserDTO> getUserBatch(@RequestBody List<Long> userIds);

}
