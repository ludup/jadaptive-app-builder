package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class HiddenFormInput extends FieldInputRender {

	public HiddenFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public final void renderInput(Element rootElement, String value, String... classes) {
	
		rootElement.appendChild(new Element("input")
						.attr("id", getFormVariable())
						.attr("name", getFormVariable())
						.addClass("form-control")
						.attr("value", value)
						.attr("type", "hidden"));
	}

}
