package com.jadaptive.api.entity;

public class EntityException extends RuntimeException {

	private static final long serialVersionUID = 1746679819167768580L;
	
	public EntityException(String msg) {
		super(msg);
	}
	
	public EntityException(Throwable e) {
		super(e.getMessage(), e);
	}

	public EntityException(String msg, Exception e) {
		super(msg, e);
	}

}
