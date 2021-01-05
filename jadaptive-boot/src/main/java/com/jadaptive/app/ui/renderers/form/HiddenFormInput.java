package com.jadaptive.app.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public class HiddenFormInput extends FieldInputRender {

	public HiddenFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}
	
	public final void renderInput(OrderedView panel, Element rootElement, String value) {
	
		rootElement.appendChild(new Element("input")
						.attr("id", field.getResourceKey())
						.attr("name", field.getResourceKey())
						.addClass("form-control")
						.attr("value", value)
						.attr("type", "hidden"));
	}

}
