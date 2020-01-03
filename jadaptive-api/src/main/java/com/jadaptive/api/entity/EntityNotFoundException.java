package com.jadaptive.api.entity;

public class EntityNotFoundException extends EntityException {

	private static final long serialVersionUID = 1L;

	public EntityNotFoundException(String msg) {
		super(msg);
	}

	public EntityNotFoundException(String msg, Exception e) {
		super(msg, e);
	}

	public EntityNotFoundException(Throwable e) {
		super(e);
	}

	
}
