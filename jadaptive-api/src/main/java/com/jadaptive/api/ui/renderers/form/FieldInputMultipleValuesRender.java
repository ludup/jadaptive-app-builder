package com.jadaptive.api.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.TemplateView;

public abstract class FieldInputMultipleValuesRender {

	protected ObjectTemplate template;
	protected TemplateViewField field;
	
	public FieldInputMultipleValuesRender(ObjectTemplate template, TemplateViewField field) {
		this.template = template;
		this.field = field;
	}
	
	public abstract void renderInput(TemplateView panel, Element rootElement, 
			Collection<String> availableValues, 
			Collection<String> selectedValues,
			boolean valueIsResourceKey);

}
