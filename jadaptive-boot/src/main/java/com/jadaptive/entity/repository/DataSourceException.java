package com.jadaptive.entity.repository;

public class DataSourceException extends Exception {

	private static final long serialVersionUID = 3065891797950652104L;

	public DataSourceException(String msg) {
		super(msg);
	}
	
	public DataSourceException(Throwable t) {
		super(t.getMessage(), t);
	}

}
