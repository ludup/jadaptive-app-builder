package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public class TextAreaFormInput extends FieldInputRender {

	public TextAreaFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}

	@Override
	public void renderInput(OrderedView panel, Element rootElement, String value) {

		
		rootElement.appendChild(new Element("div")
				.addClass("form-group")
				.addClass("w-100")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("col-form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("textarea")
						.attr("id", field.getFormVariable())
						.attr("name", field.getFormVariable())
						.addClass("form-control")
						.val(value))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey()))));


	}

}
