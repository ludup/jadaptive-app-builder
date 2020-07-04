package com.jadaptive.app.webbits;

import java.text.MessageFormat;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.i18n.DefaultBundleResolver;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;

@Component
public class WebbitsBundleResolver extends DefaultBundleResolver implements I18nService {

	@Autowired
	private ClassLoaderService classService; 
	
	@PostConstruct
	private void postConstruct() {
		setDefaultClassLoader(classService.getClassLoader());
	}
	
	@Override
	public String format(String bundle, Locale locale, String key, Object... args) {
		return MessageFormat.format(resolve(bundle, locale, null, null, key), args);
	}
	
	@Override
	public String getFieldName(ObjectTemplate template, FieldTemplate field) {
		return getFieldName(template, field, Locale.getDefault());
		
	}
	
	@Override
	public String getFieldName(ObjectTemplate template, FieldTemplate field, Locale locale) {
		return format(String.format("i18n/%s", template.getResourceKey()), 
				locale, String.format("%s.%s.name", template.getResourceKey(), field.getResourceKey()));
	}
	
	@Override
	public String getFieldDesc(ObjectTemplate template, FieldTemplate field) {
		return getFieldDesc(template, field, Locale.getDefault());
	}
	
	@Override
	public String getFieldDesc(ObjectTemplate template, FieldTemplate field, Locale locale) {
		return format(String.format("i18n/%s", template.getResourceKey()), 
				locale, String.format("%s.%s.desc", template.getResourceKey(), field.getResourceKey()));
	}
}
