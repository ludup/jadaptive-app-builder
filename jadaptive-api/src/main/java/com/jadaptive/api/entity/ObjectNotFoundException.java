package com.jadaptive.api.entity;

public class ObjectNotFoundException extends ObjectException {

	private static final long serialVersionUID = 1L;

	public ObjectNotFoundException(String msg) {
		super(msg);
	}

	public ObjectNotFoundException(String msg, Exception e) {
		super(msg, e);
	}

	public ObjectNotFoundException(Throwable e) {
		super(e);
	}

	
}
