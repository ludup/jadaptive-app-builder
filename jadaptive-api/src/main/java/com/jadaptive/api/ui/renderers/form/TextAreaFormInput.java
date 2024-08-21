package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.ui.Html;

public class TextAreaFormInput extends FieldInputRender {

	int rows;
	Element input;
	
	public TextAreaFormInput(TemplateViewField field, int rows) {
		super(field);
		this.rows = rows;
	}
	
	

	public TextAreaFormInput(String resourceKey, String formVariable, String bundle, int rows) {
		super(resourceKey, formVariable, bundle);
		this.rows = rows;
	}


	@Override
	public void renderInput(Element rootElement, String value, String... classes) {


		rootElement.appendChild(new Element("div")
				.appendChild(new Element("label")
						.attr("for", getFormVariable())
						.addClass("form-label")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.name", getResourceKey())))
				.appendChild(Html.div("input-group").appendChild(input = new Element("textarea")
						.attr("name", getFormVariableWithParents())
						.attr("rows", String.valueOf(rows))
						.addClass(getResourceKey() + " form-control")
						.val(value)))
				.appendChild(new Element("small")
						.addClass("form-text")
						.addClass("text-muted")
						.attr("jad:bundle", getBundle())
						.attr("jad:i18n", String.format("%s.desc", getResourceKey()))));

		if(!disableIDAttribute) {
			input.attr("id", getResourceKey());
		}
	}

	public Element getInputElement() {
		return input;
	}

}
