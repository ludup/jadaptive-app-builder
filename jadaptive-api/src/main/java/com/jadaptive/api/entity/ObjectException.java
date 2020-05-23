package com.jadaptive.api.entity;

public class ObjectException extends RuntimeException {

	private static final long serialVersionUID = 1746679819167768580L;
	
	public ObjectException(String msg) {
		super(msg);
	}
	
	public ObjectException(Throwable e) {
		super(e.getMessage(), e);
	}

	public ObjectException(String msg, Throwable e) {
		super(msg, e);
	}

}
