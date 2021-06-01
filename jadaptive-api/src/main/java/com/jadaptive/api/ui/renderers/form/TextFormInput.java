package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;

public class TextFormInput extends FormInputRender {

	public TextFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}

	@Override
	public String getInputType() {
		return "text";
	}

}
