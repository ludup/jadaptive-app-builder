package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class BooleanFormInput extends FieldInputRender {

	Element input;
	
	public BooleanFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	@Override
	public void renderInput(Element rootElement, String value) {
		
		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
		
		.appendChild(new Element("label")
				.attr("for", field.getFormVariable())
				.addClass("form-label")
				.attr("jad:bundle", field.getBundle())
				.attr("jad:i18n", String.format("%s.name", field.getResourceKey()))))
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("div")
						.addClass("col-12 form-check form-switch")
		
		.appendChild(input = new Element("input")
			.attr("id", field.getFormVariable())
			.attr("name", field.getFormVariable())
			.attr("type", "checkbox")
			.addClass("form-check-input")
			.val("true")))
				.appendChild(new Element("div")
						.addClass("col-12"))
		.appendChild(new Element("p")
				.addClass("form-text text-muted text-small mt-3")
				.attr("jad:bundle", field.getBundle())
				.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));

		if("true".equalsIgnoreCase(value)) {
			input.attr("checked", "checked");
		}
	}

	public void disable() {
		input.attr("disabled", "disabled");
	}

}
