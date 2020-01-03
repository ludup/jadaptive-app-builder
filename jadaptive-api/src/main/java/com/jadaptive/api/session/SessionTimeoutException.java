package com.jadaptive.api.session;

public class SessionTimeoutException extends Exception {

	private static final long serialVersionUID = -3227158318407691563L;

	public SessionTimeoutException() {
		super();
	}

	public SessionTimeoutException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SessionTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public SessionTimeoutException(String message) {
		super(message);
	}

	public SessionTimeoutException(Throwable cause) {
		super(cause);
	}

	
}
