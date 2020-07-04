package com.jadaptive.app.ui.renderers.form;

import org.jsoup.select.Elements;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;

public abstract class FieldInputRender {

	protected ObjectTemplate template;
	protected FieldTemplate field;
	
	public FieldInputRender(ObjectTemplate template, FieldTemplate field) {
		this.template = template;
		this.field = field;
	}
	
	public abstract void renderInput(Elements rootElement, String value);
	
	protected String replaceResourceKey(String str) {
		return str.replace("${i18nName}", 
			"webbits:bundle=\"i18n/${templateResourceKey}\" webbits:i18n=\"${templateResourceKey}.${resourceKey}.name\"")
				.replace("${i18nDesc}", 
						"webbits:bundle=\"i18n/${templateResourceKey}\" webbits:i18n=\"${templateResourceKey}.${resourceKey}.desc\"")
		 		.replace("${resourceKey}", field.getResourceKey())
				.replace("${templateResourceKey}", template.getResourceKey());
	}
}
