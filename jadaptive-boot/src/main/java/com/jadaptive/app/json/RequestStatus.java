package com.jadaptive.app.json;

public class RequestStatus {

	boolean success;
	String message;
	
	public RequestStatus() {
		this.success = true;
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
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
