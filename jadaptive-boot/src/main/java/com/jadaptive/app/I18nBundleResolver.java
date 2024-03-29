package com.jadaptive.app;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.app.I18N;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;

@Service
public class I18nBundleResolver implements I18nService {

	@Override
	public String format(String bundle, Locale locale, String key, Object... args) {
		switch(bundle) {
		case "app":
		case "vendor":
		{
			ProductService service = ApplicationServiceImpl.getInstance().getBean(ProductService.class);
			switch(key) {
			case "version.text":
			case "product.version":
				return service.getVersion();
			case "product.copyright":
				return service.getCopyright();
			default:
				return I18N.getResource(locale, bundle, key, args);
			}
		}
		default:
			return I18N.getResource(locale, bundle, key, args);
		}
		
	}
	
	@Override
	public String formatNoDefault(String bundle, Locale locale, String key, Object... args) {
		switch(bundle) {
		case "app":
		case "vendor":
		{
			ProductService service = ApplicationServiceImpl.getInstance().getBean(ProductService.class);
			switch(key) {
			case "version.text":
			case "product.version":
				return service.getVersion();
			case "product.copyright":
				return service.getCopyright();
			default:
				return I18N.getResourceNoDefault(locale, bundle, key, args);
			}
		}
		default:
			return I18N.getResourceNoDefault(locale, bundle, key, args);
		}
		
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
