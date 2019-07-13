package com.jadaptive.entity;

public class EntityException extends Exception {

	private static final long serialVersionUID = 1746679819167768580L;
	
	public EntityException(String msg) {
		super(msg);
	}

	public EntityException(String msg, Exception e) {
		super(msg, e);
	}

}
