package com.jadaptive.app.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public class BooleanFormInput extends FieldInputRender {

	public BooleanFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}

	@Override
	public void renderInput(OrderedView panel, Element rootElement, String value) {
		
		Element input;
		rootElement.appendChild(new Element("div")
						.addClass("form-group col-12")
				.appendChild(input = new Element("input")
						.attr("id", field.getFormVariable())
						.attr("name", field.getFormVariable())
						.attr("type", "checkbox")
						.val("true"))
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("col-form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey()))));

		if("true".equalsIgnoreCase(value)) {
			input.attr("checked", "checked");
		}
	}

}
