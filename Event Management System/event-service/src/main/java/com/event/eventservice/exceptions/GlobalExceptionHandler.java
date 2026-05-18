package com.event.eventservice.exceptions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
		logger.error("Unhandled exception occurred", ex);
	    return ResponseEntity
	            .status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(new ErrorResponse(
	                    "Internal Server Error",
	                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
	                    LocalDateTime.now()
	            ));
	}
	@ExceptionHandler(EventNotFoundException.class)
	public ResponseEntity<ErrorResponse>handleNotFound(EventNotFoundException ex){
		logger.warn("Event not found exception: {}", ex.getMessage());
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse(
						ex.getMessage(),
						404,
						LocalDateTime.now()
				));
	}
	@ExceptionHandler(InvalidEventException.class)
	public ResponseEntity<ErrorResponse> handleInvalidEvent(InvalidEventException ex) {
		logger.warn("Invalid event exception: {}", ex.getMessage());
	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(new ErrorResponse(
	                    ex.getMessage(),
	                    400,
	                    LocalDateTime.now()
	            ));
	}
	@ExceptionHandler(EventAlreadyDeletedException.class)
	public ResponseEntity<ErrorResponse> handleEventAlreadyDeleted(EventAlreadyDeletedException ex) {
		logger.warn("Event already deleted exception: {}", ex.getMessage());
	    return ResponseEntity
	            .status(HttpStatus.NOT_FOUND)
	            .body(new ErrorResponse(
	                    ex.getMessage(),
	                    404,
	                    LocalDateTime.now()
	            ));
	}
	@ExceptionHandler(InsufficientSeatsException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientSeats(InsufficientSeatsException ex) {
		logger.warn("Insufficient seats exception: {}", ex.getMessage());
	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(new ErrorResponse(
	                    ex.getMessage(),
	                    409,
	                    LocalDateTime.now()
	            ));
	}
	@ExceptionHandler(SeatCapacityException.class)
	public ResponseEntity<ErrorResponse> handleSeatCapacity(SeatCapacityException ex) {
		logger.warn("Seat capacity exception: {}", ex.getMessage());
	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(new ErrorResponse(
	                    ex.getMessage(),
	                    400,
	                    LocalDateTime.now()
	            ));
	}
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
	    // Collect all error messages into one string
	    String errorMessage = ex.getBindingResult().getFieldErrors()
	            .stream()
	            .map(error -> error.getField() + ": " + error.getDefaultMessage())
	            .collect(Collectors.joining(", "));
	    
	    logger.warn("Validation failed: {}", errorMessage);

	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(new ErrorResponse(
	                    "Validation Failed: " + errorMessage,
	                    400,
	                    LocalDateTime.now()
	            ));
	}
}
