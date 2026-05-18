package com.event.userservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import com.event.userservice.clients.NotificationClient;
import com.event.userservice.dto.UserDTO;
import com.event.userservice.entity.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.event.userservice.entity.User;
import com.event.userservice.entity.UserStatus;
import com.event.userservice.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private UserController userController;

    @Test
    void testRegister_Success() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setEmail("new@event.com");
        user.setFullName("New User");
        user.setRole(Role.ROLE_ATTENDEE);
        user.setStatus(UserStatus.ACTIVE);

        when(userService.registerUser(any(User.class))).thenReturn(user);

        // Act
        ResponseEntity<UserDTO> response = userController.register(user);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new@event.com", response.getBody().getEmail());
        assertNull(response.getBody().getContactNumber());
        verify(userService, times(1)).registerUser(any());
    }

    @Test
    void testApproveOrganizer_Success() {
        // Arrange
        User approvedUser = new User();
        approvedUser.setUserId(1L);
        approvedUser.setEmail("org@event.com");
        approvedUser.setFullName("Organizer");
        approvedUser.setRole(Role.ROLE_ORGANIZER);
        approvedUser.setStatus(UserStatus.ACTIVE);

        when(userService.approveOrganizer(1L)).thenReturn(approvedUser);

        // Act
        ResponseEntity<UserDTO> response = userController.approve(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UserStatus.ACTIVE, response.getBody().getStatus());
        verify(userService, times(1)).approveOrganizer(1L);
    }

    @Test
    void testGetPending_ReturnsList() {
        // Arrange
        User u1 = new User();
        u1.setUserId(2L);
        u1.setEmail("pending@event.com");
        u1.setFullName("Pending Org");
        u1.setRole(Role.ROLE_ORGANIZER);
        u1.setStatus(UserStatus.PENDING);

        when(userService.getPendingOrganizers()).thenReturn(Arrays.asList(u1));

        // Act
        ResponseEntity<List<UserDTO>> response = userController.getPending();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(UserStatus.PENDING, response.getBody().get(0).getStatus());
        verify(userService, times(1)).getPendingOrganizers();
    }

    @Test
    void testGetAllUsers_ReturnsDTOList() {
        User u1 = new User();
        u1.setUserId(1L);
        u1.setEmail("a@event.com");
        u1.setFullName("User A");
        u1.setRole(Role.ROLE_ATTENDEE);
        u1.setStatus(UserStatus.ACTIVE);

        when(userService.findAllUsers()).thenReturn(Arrays.asList(u1));

        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("a@event.com", response.getBody().get(0).getEmail());
        verify(userService, times(1)).findAllUsers();
    }
    @Test
    void testGetUserById_ReturnsDTO() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("john@event.com");
        user.setFullName("John");
        user.setRole(Role.ROLE_ATTENDEE);
        user.setStatus(UserStatus.ACTIVE);

        when(userService.findById(1L)).thenReturn(user);

        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("john@event.com", response.getBody().getEmail());
        verify(userService, times(1)).findById(1L);
    }

    @Test
    void testGetUsersByIds_ReturnsDTOList() {
        User u1 = new User();
        u1.setUserId(1L);
        u1.setEmail("a@event.com");
        u1.setFullName("User A");
        u1.setRole(Role.ROLE_ATTENDEE);
        u1.setStatus(UserStatus.ACTIVE);

        when(userService.findById(1L)).thenReturn(u1);

        ResponseEntity<List<UserDTO>> response =
                userController.getUsersByIds(Arrays.asList(1L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(userService, times(1)).findById(1L);
    }
    @Test
    void testGetUsersByIds_SkipsMissingUser() {
        when(userService.findById(99L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<List<UserDTO>> response =
                userController.getUsersByIds(Arrays.asList(99L));

        // Should return empty list — not throw exception
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size()); // ✅ skipped, not crashed
    }
}