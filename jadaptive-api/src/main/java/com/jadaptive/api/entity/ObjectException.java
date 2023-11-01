package com.jadaptive.api.entity;

import java.util.Locale;

import com.jadaptive.api.app.I18N;
import com.jadaptive.api.servlet.Request;

public class ObjectException extends RuntimeException {

	private static final long serialVersionUID = 1746679819167768580L;
	
	public ObjectException(String bundle, String key, Object... args) {
		super(generateI18nText(bundle, key, args));
	}
	
	public ObjectException(String msg) {
		super(msg);
	}
	
	public ObjectException(Throwable e) {
		super(e.getMessage(), e);
	}

	public ObjectException(String msg, Throwable e) {
		super(msg, e);
	}

	private static String generateI18nText(String bundle, String key, Object... args) {
		
		Locale locale = Locale.getDefault();
		if(Request.isAvailable()) {
			locale = Request.get().getLocale();
		}
		return I18N.getResource(locale, bundle, key, args);
	}
}
