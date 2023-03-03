package com.jadaptive.api.json;

public class EntityStatus<T> extends RequestStatusImpl {

	T resource;
	
	public EntityStatus(T resource) {
		this.resource = resource;
	}

	public EntityStatus(boolean success, String message) {
		super(success, message);
	}

	public T getResource() {
		return resource;
	}

	public void setResource(T resource) {
		this.resource = resource;
	}
	
	

}
