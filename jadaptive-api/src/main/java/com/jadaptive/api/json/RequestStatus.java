package com.jadaptive.api.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class RequestStatus {

	private boolean success = true;
	private String message = "";
	
	public RequestStatus() {
		
	}
	
	public RequestStatus(boolean success) {
		this.success = success;
		this.message = "";
	}
	
	public RequestStatus(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String errorMsg) {
		this.message = errorMsg;
	}
	
	
}
