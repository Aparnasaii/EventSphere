package com.event.userservice.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testUserNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User 1 missing"));
    }

    @Test
    void testUserAlreadyExists_Returns409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void testInvalidToken_Returns400() throws Exception {
        mockMvc.perform(get("/test/invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void testGeneralException_Returns500() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @RestController
    private static class ExceptionTestController {

        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new UserNotFoundException("User 1 missing");
        }

        @GetMapping("/test/conflict")
        public void throwConflict() {
            throw new UserAlreadyExistsException("john@gmail.com", "email");
        }

        @GetMapping("/test/invalid-token")
        public void throwInvalidToken() {
            throw new InvalidTokenException("Token has expired");
        }

        @GetMapping("/test/error")
        public void throwGeneral() throws Exception {
            throw new Exception("Unexpected crash");
        }
    }
}