package com.jadaptive.api.tasks;

import java.io.Serializable;

public class FeedbackMessage implements Serializable {

	private static final long serialVersionUID = -5052079186197815096L;
	FeedbackStatus status;
	String message;
	boolean done;
	
	public FeedbackMessage() {
		
	}
	
	public FeedbackMessage(FeedbackStatus status, String message, boolean done) {
		this.status = status;
		this.message = message;
		this.done = done;
	}
	
	public FeedbackStatus getStatus() {
		return status;
	}
	
	public void setStatus(FeedbackStatus status) {
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
	
}
