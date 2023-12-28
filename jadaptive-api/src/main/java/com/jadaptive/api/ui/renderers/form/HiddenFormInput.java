package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class HiddenFormInput extends FieldInputRender {

	public HiddenFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public final void renderInput(Element rootElement, String value, String... classes) {
	
		Element input;
		rootElement.appendChild(input = new Element("input")
						.attr("name", getFormVariable())
						.addClass(resourceKey + " form-control")
						.attr("value", value)
						.attr("type", "hidden"));
		
		if(!disableIDAttribute) {
			input.attr("id", resourceKey);
		}
	}

}
