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
	public void renderInput(Element rootElement, String value, String... classes) {

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
				.appendChild(input = new Element("textarea")
						.attr("name", getFormVariableWithParents())
						.attr("rows", String.valueOf(rows))
						.addClass(getResourceKey() + " form-control")
						.val(value))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.desc", getResourceKey())))));

		if(!disableIDAttribute) {
			input.attr("id", getResourceKey());
		}
	}

}
