package com.event.eventservice.exceptions;

public class EventAlreadyDeletedException extends RuntimeException {
	public EventAlreadyDeletedException(Long id) {
		super("Event already delered with id:"+id);
	}

}
