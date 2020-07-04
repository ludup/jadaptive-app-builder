package com.jadaptive.app.ui.renderers.form;

import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;

public class TextFormInput extends FormInputRender {

	public TextFormInput(ObjectTemplate template, FieldTemplate field) {
		super(template, field);
	}

	@Override
	public String getInputType() {
		return "text";
	}

}
