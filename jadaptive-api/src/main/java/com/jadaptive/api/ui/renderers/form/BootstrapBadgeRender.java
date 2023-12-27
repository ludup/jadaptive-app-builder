package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class BootstrapBadgeRender extends FieldInputRender {
	
	public BootstrapBadgeRender(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}
	
	public final void renderInput(Element rootElement, String value, String... classes) {
		

		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(generateBadge(value))
				.appendChild(new Element("input")
						.attr("id", getFormVariable())
						.attr("name", getFormVariableWithParents())
						.addClass("form-control")
						.attr("value", value)
						.attr("type", "hidden"))));

	}

	public static Element generateBadge(String value) {
		return new Element("span")
				.addClass(String.format("badge bg-%s p-2", getBadgeClass(value)))
				.text(value);
	}
	
	private static Object getBadgeClass(String value) {
		switch(value.toUpperCase()) {
		case "SUCCESS":
			return "success";
		case "ERROR":
			return "danger";
		case "WARNING":
			return "warning";
		default:
			return "info";
		}
	}


}
