package com.event.userservice.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public ResetPasswordRequest(String token, String newPassword) {
		super();
		this.token = token;
		this.newPassword = newPassword;
	}
	public ResetPasswordRequest() {
		super();
	}
    
    
}