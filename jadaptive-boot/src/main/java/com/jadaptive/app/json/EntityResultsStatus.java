package com.jadaptive.app.json;

public class EntityResultsStatus<T> extends RequestStatus {

	Iterable<T> resource;
	
	public EntityResultsStatus(Iterable<T> resource) {
		this.resource = resource;
	}

	public EntityResultsStatus(boolean success, String message) {
		super(success, message);
	}

	public Iterable<T> getResource() {
		return resource;
	}

	public void setResource(Iterable<T> resource) {
		this.resource = resource;
	}
	
	

}
