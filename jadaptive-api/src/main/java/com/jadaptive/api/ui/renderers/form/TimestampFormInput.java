package com.jadaptive.api.ui.renderers.form;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.utils.Utils;

public class TimestampFormInput extends FieldInputRender {

	public TimestampFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	public final void renderInput(Element rootElement, String value, String... classes) {
		
		Element input;
		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())))
				.appendChild(new Element("input")
						.addClass("form-control")
						.attr("value", StringUtils.isNotBlank(value) ? Utils.parseTimestamp(value).toString() : "")
						.attr("autocomplete", "off")
						.attr("type", "text"))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.desc", getResourceKey())))));
		
		
		rootElement.appendChild(input = new Element("input")
				.attr("name", getFormVariableWithParents())
				.addClass(getResourceKey() + "form-control")
				.attr("value", value)
				.attr("type", "hidden"));
		
		if(!disableIDAttribute) {
			input.attr("id", getResourceKey());
		}
	}

	
}
