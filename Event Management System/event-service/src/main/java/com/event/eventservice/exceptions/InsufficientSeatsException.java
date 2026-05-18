package com.event.eventservice.exceptions;

public class InsufficientSeatsException extends RuntimeException{
	
	public InsufficientSeatsException() {
		super("Not enough seats available");
	}
	public InsufficientSeatsException(int requested,int available) {
		super("Requested" + requested+"seats but only "+available+ " available");
	}
}
