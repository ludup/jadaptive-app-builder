package com.jadaptive.app.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public abstract class FieldInputRender {

	protected ObjectTemplate template;
	protected OrderedField field;

	public FieldInputRender(ObjectTemplate template, OrderedField field) {
		this.template = template;
		this.field = field;
	}
	
	public abstract void renderInput(OrderedView panel, Element rootElement, String value);

}
