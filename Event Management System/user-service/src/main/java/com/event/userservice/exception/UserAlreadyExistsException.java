package com.event.userservice.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String email, String field) {
        super("User already exists with " + field + ": " + email);
    }
}