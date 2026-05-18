package com.event.userservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice // Combines @ControllerAdvice and @ResponseBody
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Standard helper to build the JSON response body
    private Map<String, Object> createErrorBody(String error, String message, HttpStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }

    // 1. Handle "Not Found" (404)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        logger.error("User not found: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody("Not Found", ex.getMessage(), HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    // 2. Handle "Conflict" (409)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserConflict(UserAlreadyExistsException ex) {
        logger.warn("Registration conflict: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody("Conflict", ex.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    // 3. Handle "Bad Request" (400) - Useful for your InvalidTokenException
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        logger.warn("Token error: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorBody("Bad Request", ex.getMessage(), HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    // 4. Catch-all for any other unexpected errors (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        logger.error("Internal Server Error: ", ex);
        return new ResponseEntity<>(createErrorBody("Internal Server Error", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}