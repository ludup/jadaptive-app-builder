package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public class BooleanFormInput extends FieldInputRender {

	Element input;
	
	public BooleanFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}

	@Override
	public void renderInput(OrderedView panel, Element rootElement, String value) {
		

		rootElement.appendChild(new Element("div")
						.addClass("row mb-3")
						.appendChild(new Element("div")
								.addClass("col-12")
				.appendChild(input = new Element("input")
						.attr("id", field.getFormVariable())
						.attr("name", field.getFormVariable())
						.attr("type", "checkbox")
						.val("true"))
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("p")
						.addClass("form-text")
						.addClass("text-muted text-small")
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