package com.jadaptive.api.ui.renderers.form;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jsoup.nodes.Element;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;

public class OptionsFormInput {

	Element input;
	TemplateViewField field;
	public OptionsFormInput(TemplateViewField field) {
		this.field = field;
	}

	public void renderInput(Element rootElement, Collection<AbstractObject> values, Iterable<AbstractObject> options, String... ignore) {
		
		List<String> ignoreValues = Arrays.asList(ignore);
		
		Element optionsElement;
		rootElement.appendChild(optionsElement =new Element("div")
				.addClass("row"));
		
		optionsElement.appendChild(Html.div("col-12 mb-3")
				.appendChild(Html.i18n(field.getBundle(), String.format("%s.name", field.getResourceKey()))));
		for(AbstractObject option : options) {
			if(ignoreValues.contains(option.getUuid())) {
				continue;
			}
			optionsElement.appendChild(new Element("div")
					.addClass("col-md-3")
					.appendChild(input = new Element("input")
					.attr("name", field.getFormVariable())
					.attr("type", "checkbox")
					.val(option.getUuid()))
						.appendChild(new Element("label")
								.attr("for", field.getFormVariable())
								.addClass("form-label")
								.text((String)option.getValue("name"))));
		
			if(values.contains(option)) {
				input.attr("checked", "checked");
			}
		}
		
		optionsElement.after(Html.div("row").appendChild(new Element("span")
				.addClass("form-text text-muted text-small mb-5")
				.attr("jad:bundle", field.getBundle())
				.attr("jad:i18n", String.format("%s.desc", field.getResourceKey()))));

		
	}

	public void disable() {
		input.attr("disabled", "disabled");
	}

}
