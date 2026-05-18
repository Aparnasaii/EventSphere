package com.event.userservice.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public ForgotPasswordRequest() {
		super();
	}

	public ForgotPasswordRequest(String email) {
		super();
		this.email = email;
	}
    
    
}