package com.jadaptive.api.ui.renderers.form;

import java.io.IOException;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.TemplateView;

public abstract class FieldInputRender {

	protected ObjectTemplate template;
	protected TemplateViewField field;

	public FieldInputRender(ObjectTemplate template, TemplateViewField field) {
		this.template = template;
		this.field = field;
	}
	
	protected String getFormVariable() {
		return field.getFormVariable();
	}
	protected String getBundle() {
		return field.getBundle();
	}
	protected String getResourceKey() {
		return field.getResourceKey();
	}
	
	public abstract void renderInput(TemplateView panel, Element rootElement, String value) throws IOException;

}
