package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;

public class BootstrapBadgeRender extends FieldInputRender {
	
	public BootstrapBadgeRender(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}
	
	public final void renderInput(OrderedView panel, Element rootElement, String value) {
		

		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("input")
						.addClass("badge bg-success")
						.text(value))
				.appendChild(new Element("input")
						.attr("id", field.getFormVariable())
						.attr("name", field.getFormVariable())
						.addClass("form-control")
						.attr("value", value)
						.attr("type", "hidden"))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));

	}


}
