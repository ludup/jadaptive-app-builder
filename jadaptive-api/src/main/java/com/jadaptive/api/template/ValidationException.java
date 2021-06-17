package com.jadaptive.api.template;

import java.util.Locale;

import com.jadaptive.api.app.I18N;

public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 2501683254211454421L;

	public ValidationException() {
		super();
	}
	
	public ValidationException(String bundle, String key, Object... arguments) {
		super(I18N.getResource(Locale.getDefault(), bundle, key, arguments));
	}

	public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

}
