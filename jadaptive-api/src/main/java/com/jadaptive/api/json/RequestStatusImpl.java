package com.jadaptive.api.json;

public class RequestStatusImpl implements RequestStatus {

	boolean success;
	String message;
	
	public RequestStatusImpl() {
		this.success = true;
		this.message = "";
	}
	
	public RequestStatusImpl(boolean success) {
		this(success, "");
	}
	
	public RequestStatusImpl(boolean success, String message) {
		super();
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
