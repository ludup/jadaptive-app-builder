package com.jadaptive.api.ui.renderers.form;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateViewField;

public class NumberFormInput extends FormInputRender {

	public NumberFormInput(ObjectTemplate template, TemplateViewField field) {
		super(template, field);
	}

	@Override
	public String getInputType() {
		return "number";
	}

		
	
	
}
