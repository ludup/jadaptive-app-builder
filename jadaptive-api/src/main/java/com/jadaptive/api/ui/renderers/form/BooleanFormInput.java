package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.utils.Utils;

public class BooleanFormInput extends FieldInputRender {

	Element input;
	
	public BooleanFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	public BooleanFormInput(ObjectTemplate template, String resourceKey, String formVariable, String bundle) {
		super(template, resourceKey, formVariable, bundle);
	}

	@Override
	public void renderInput(Element rootElement, String value, String... classes) {
		
		Element e;
		
		rootElement.appendChild(e = new Element("div")
				.addClass(Utils.csv(" ", classes) + " row mb-3"))
				.appendChild(new Element("div")
						.addClass("col-12"));
		
		if(decorate) {
			e.appendChild(new Element("label")
				.attr("for", getFormVariable())
				.addClass("form-label")
				.attr("jad:bundle", getBundle())
				.attr("jad:i18n", String.format("%s.name", getResourceKey())));
		}
		
		e.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("div")
						.addClass("col-12 form-check form-switch")
				.appendChild(input = new Element("input")
					.attr("id", resourceKey)
					.attr("name", getFormVariable())
					.attr("type", "checkbox")
					.addClass("form-check-input")
					.val("true"))));
		
		if(decorate) {
				e.appendChild(new Element("div")
						.addClass("col-12"))
				.appendChild(new Element("p")
				.addClass("form-text text-muted text-small mt-3")
				.attr("jad:bundle", getBundle())
				.attr("jad:i18n", String.format("%s.desc", getResourceKey())));
		}

		if("true".equalsIgnoreCase(value)) {
			input.attr("checked", "checked");
		}
	}

	public void disable() {
		input.attr("disabled", "disabled");
	}

}
