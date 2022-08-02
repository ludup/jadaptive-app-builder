package com.jadaptive.api.ui.renderers.form;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.utils.Utils;

public class TimestampFormInput extends FieldInputRender {

	public TimestampFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}

	public final void renderInput(OrderedView panel, Element rootElement, String value) {
		
		rootElement.appendChild(new Element("div")
				.addClass("row mb-3")
				.appendChild(new Element("div")
						.addClass("col-12")
				.appendChild(new Element("label")
						.attr("for", field.getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("input")
						.addClass("form-control")
						.attr("value", StringUtils.isNotBlank(value) ? Utils.parseTimestamp(value).toString() : "")
						.attr("autocomplete", "off")
						.attr("type", "text"))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));
		

		rootElement.appendChild(new Element("input")
				.attr("id", field.getFormVariable())
				.attr("name", field.getFormVariable())
				.addClass("form-control")
				.attr("value", value)
				.attr("type", "hidden"));
	}

	
}
