package com.jadaptive.api.permissions;

public class AccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = -1051452410940869534L;

	public AccessDeniedException() {
		super("Access Denied");
	}

	public AccessDeniedException(String message) {
		super(message);
	}

	public AccessDeniedException(Throwable cause) {
		super(cause);
	}

	public AccessDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccessDeniedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
