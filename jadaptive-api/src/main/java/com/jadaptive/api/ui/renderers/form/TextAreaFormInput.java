package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class TextAreaFormInput extends FieldInputRender {

	int rows;
	public TextAreaFormInput(ObjectTemplate template, TemplateViewField field, int rows) {
		super(template, field);
		this.rows = rows;
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
						.attr("jad:i18n", String.format("%s.name", field.getResourceKey())))
				.appendChild(new Element("textarea")
						.attr("id", field.getFormVariable())
						.attr("name", field.getFormVariable())
						.attr("rows", String.valueOf(rows))
						.addClass("form-control")
						.val(value))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", field.getBundle())
						.attr("jad:i18n", String.format("%s.desc", field.getResourceKey())))));


	}

}
