package com.jadaptive.api.ui.renderers.form;

import org.jsoup.nodes.Element;

import com.jadaptive.api.template.TemplateViewField;

public class TextAreaFormInput extends FieldInputRender {

	private int rows;
	private Element input;

	public TextAreaFormInput(TemplateViewField field, int rows) {
		super(field);
		this.rows = rows;
	}



	public TextAreaFormInput(String resourceKey, String formVariable, String bundle, int rows) {
		super(resourceKey, formVariable, bundle);
		this.rows = rows;
	}


	@Override
	protected void onRender(Element rootElement, String value, boolean readOnly, String... classes) {

		elementForRole(rootElement, "label")
				.attr("for", getFormVariable())
				.attr("jad:bundle", getBundle())
				.attr("jad:i18n", String.format("%s.name", getResourceKey()));

		input = elementForRole(rootElement, "input")
				.attr("name", getFormVariableWithParents())
				.attr("rows", String.valueOf(rows))
				.addClass(getResourceKey() + " form-control")
				.val(value);

		elementForRole(rootElement, "help")
				.attr("jad:bundle", getBundle())
				.attr("jad:i18n", String.format("%s.desc", getResourceKey()));

		if(!disableIDAttribute) {
			input.attr("id", getResourceKey());
		}
	}

	public Element getInputElement() {
		return input;
	}

}
