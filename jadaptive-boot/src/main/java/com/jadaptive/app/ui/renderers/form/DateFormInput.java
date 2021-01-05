package com.jadaptive.app.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedField;

public class DateFormInput extends FormInputRender {

	public DateFormInput(ObjectTemplate template, OrderedField field) {
		super(template, field);
	}
	
	@Override
	public String getInputType() {
		return "date";
	}
}
