package com.jadaptive.app.json;

public class EntityStatus<T> extends RequestStatus {

	T result;
	
	public EntityStatus(T result) {
		this.result = result;
	}

	public EntityStatus(boolean success, String message) {
		super(success, message);
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}
	
	

}
