package com.jadaptive.app.ui.renderers.form;

import java.util.Collection;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public abstract class FieldInputMultipleValuesRender {

	protected ObjectTemplate template;
	protected OrderedField field;
	
	public FieldInputMultipleValuesRender(ObjectTemplate template, OrderedField field) {
		this.template = template;
		this.field = field;
	}
	
	public abstract void renderInput(OrderedView panel, Element rootElement, 
			Collection<String> availableValues, 
			Collection<String> selectedValues,
			boolean valueIsResourceKey);

}
