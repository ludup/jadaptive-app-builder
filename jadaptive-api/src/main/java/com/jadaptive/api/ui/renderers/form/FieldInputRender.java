package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public abstract class FieldInputRender {

	protected ObjectTemplate template;

	TemplateViewField field;
	String resourceKey;
	String formVariable;
	String bundle;
	
	public FieldInputRender(ObjectTemplate template, TemplateViewField field) {
		this.template = template;
		this.field = field;
		this.resourceKey = field.getResourceKey();
		this.formVariable = field.getFormVariable();
		this.bundle = field.getBundle();
	}
	
	protected String getFormVariable() {
		return formVariable;
	}
	protected String getBundle() {
		return bundle;
	}
	protected String getResourceKey() {
		return resourceKey;
	}
	
	public abstract void renderInput(Element rootElement, String value) throws IOException;

}
