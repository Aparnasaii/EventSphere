package com.event.eventservice.exceptions;

public class EventNotFoundException extends RuntimeException{
	public EventNotFoundException(Long id) {
		super("Event not Found with id:"+id);
	}
	public EventNotFoundException(String message) {
		super(message);
	}

}
