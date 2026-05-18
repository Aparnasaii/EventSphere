package com.event.eventservice.exceptions;

public class SeatCapacityException extends RuntimeException {
	public SeatCapacityException() {
		super("More than seats are being added");
	}
}

