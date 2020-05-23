package com.jadaptive.app.ui.renderers.form;

import org.jsoup.select.Elements;

import com.jadaptive.api.template.FieldDefinition;

public abstract class FormInputRender {

	FieldDefinition template;
	String defaultValue;
	
	public FormInputRender(Elements rootElement, FieldDefinition template, String defaultValue) {
		this.template = template;
		this.defaultValue = defaultValue;
		renderInput(rootElement, template, defaultValue);
	}
	
	protected abstract void renderInput(Elements rootElement, FieldDefinition template, String defaultValue);

	protected String replaceResourceKey(String str) {
		return str.replace("${resourceKey}", template.getResourceKey());
	}
}
