package com.jadaptive.api.i18n;

import java.util.Locale;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;

public interface I18nService {

	String format(String bundle, Locale locale, String key, Object... args);

	String getFieldName(ObjectTemplate template, FieldTemplate field);
	
	String getFieldDesc(ObjectTemplate template, FieldTemplate field);

	String getFieldName(ObjectTemplate template, FieldTemplate field, Locale locale);

	String getFieldDesc(ObjectTemplate template, FieldTemplate field, Locale locale);
}
