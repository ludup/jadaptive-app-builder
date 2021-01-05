package com.jadaptive.app.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;

public class NumberFormInput extends FormInputRender {

	public NumberFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}

	@Override
	public String getInputType() {
		return "number";
	}

		
	
	
}
