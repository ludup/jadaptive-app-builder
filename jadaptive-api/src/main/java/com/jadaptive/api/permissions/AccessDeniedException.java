package com.jadaptive.api.permissions;

import java.util.Locale;

import com.jadaptive.api.app.I18N;
import com.jadaptive.api.servlet.Request;

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
	
	public AccessDeniedException(String bundle, String key, Object... args) {
		super(generateI18nText(bundle, key, args));
	}
	
	private static String generateI18nText(String bundle, String key, Object... args) {
		
		Locale locale = Locale.getDefault();
		if(Request.isAvailable()) {
			locale = Request.get().getLocale();
		}
		return I18N.getResource(locale, bundle, key, args);
	}

}
